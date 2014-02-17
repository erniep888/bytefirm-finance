/*
 * Copyright 2014 Byte Firm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytefirm.finance

import com.bytefirm.finance.domain.MonthlyAccountTotal
import com.bytefirm.finance.domain.Account
import com.bytefirm.finance.domain.MonthlyWhereClause
import com.bytefirm.finance.domain.WellsFargoCheckingTransaction

import javax.persistence.*
import org.hibernate.cfg.*
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.hibernate.SessionFactory
import org.hibernate.Query

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.text.DecimalFormat


/**
 * Created by ernes_000 on 2/17/14.
 */
class MonthlyAccountTool {

    static showProcessedTransactions(factory, year, month){
        def map = new HashMap<Account, List<WellsFargoCheckingTransaction>>()


        def session = factory.currentSession
        def tx = session.beginTransaction()

        def accounts = session.createQuery("from Account acct order by acct.displayOrder asc").list()

        for (Account account : accounts){
            def accountWhereClausesByMonth = session.createCriteria(MonthlyWhereClause.class)
                    .add(Restrictions.eq("account", account))
                    .add(Restrictions.eq("year", year))
                    .add(Restrictions.eq("month", month)).list()
            if (accountWhereClausesByMonth.size() > 0){
                def totalMonthlyAccountAmount = new Double(0)
                def transactionArrayList = new ArrayList<WellsFargoCheckingTransaction>()
                for (def monthlyWhereClause : accountWhereClausesByMonth){
                    def selectStatement = "from WellsFargoCheckingTransaction trans where (" + monthlyWhereClause.whereClause +
                            ") and (trans.transactionDate >= :startDate and trans.transactionDate < :endDate)"
                    def selectQuery = session.createQuery(selectStatement)
                    selectQuery = buildDateWhereClause(year, month, selectQuery)
                    def transactionList = selectQuery.list()
                    transactionList.each {
                        it.adjustedAmount = it.amount * monthlyWhereClause.amountPercentage
                        transactionArrayList.add(it)
                    }
                }
                map.put(account, transactionArrayList)
                session.flush()
                session.clear()
            }
        }
        tx.commit()

        for(def key : map.keySet()){
            def sum = new Double(0)
            println key
            for(def transaction : map[key].listIterator()){
                println ("\t" + transaction)
                sum += transaction.adjustedAmount
            }
            println ("\t\tTotal: $sum")
        }
    }

    static double getBalanceDelta(factory, year, month){
        def balanceDelta = new Double(0)
        def rawBalanceSum = new Double(0)
        double balanceChange = getMonthlyBalanceChange(factory, year, month)
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def queryString = "select sum(trans.amount) from WellsFargoCheckingTransaction trans where " +
                "(trans.transactionDate >= :startDate and trans.transactionDate < :endDate)"
        def selectQuery = session.createQuery(queryString)
        selectQuery = buildDateWhereClause(year, month, selectQuery)
        def sumResult = selectQuery.list()
        if (sumResult.size() > 0){
            rawBalanceSum = sumResult.get(0)
            balanceDelta = rawBalanceSum - balanceChange
        }

        tx.commit()

        def decimalFormat = new DecimalFormat("###.##")
        def formatedBalanceChange = decimalFormat.format(balanceChange)
        def formatedRawBalanceSum = decimalFormat.format(rawBalanceSum)

        println ("Processed transaction sum: $formatedBalanceChange   Raw transaction sum: $formatedRawBalanceSum ")

        return new Double(decimalFormat.format(balanceDelta))
    }

    static double getMonthlyBalanceChange(factory, year, month){
        def balanceChange = new Double(0)
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def queryString = "select sum(monthlyTotal.total) from MonthlyAccountTotal as monthlyTotal " +
                " where monthlyTotal.year = :year and monthlyTotal.month = :month"
        def selectQuery = session.createQuery(queryString)
        selectQuery.setParameter("year", year)
        selectQuery.setParameter("month", month)
        def totalChangeForMonth = selectQuery.list()

        if (totalChangeForMonth.size() > 0)
            balanceChange = totalChangeForMonth.get(0)
        tx.commit()

        return balanceChange
    }

    static displayMonthlyAccountTotal(factory, year, month){
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def queryString = "select monthlyTotal from MonthlyAccountTotal as monthlyTotal " +
                "inner join monthlyTotal.account as acct where monthlyTotal.year = :year and " +
                "monthlyTotal.month = :month order by acct.displayOrder asc"
        def selectQuery = session.createQuery(queryString)
        selectQuery.setParameter("year", year)
        selectQuery.setParameter("month", month)
        def monthlyAccountTotals = selectQuery.list()

        monthlyAccountTotals.each{
            println it.account.title + " " + it.total
        }


        tx.commit()
    }

    static processMonthlyTransactions(factory, year, month){
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def deleteQuery = session.createQuery("delete MonthlyAccountTotal where year = :year and month = :month");
        deleteQuery.setParameter("year", year);
        deleteQuery.setParameter("month", month);
        int deleteResult = deleteQuery.executeUpdate();

        println 'Delete *************** ' + deleteResult

        def accounts = session.createQuery("from Account acct").list()

        for (Account account : accounts){
            def accountWhereClausesByMonth = session.createCriteria(MonthlyWhereClause.class)
                    .add(Restrictions.eq("account", account))
                    .add(Restrictions.eq("year", year))
                    .add(Restrictions.eq("month", month)).list()
            if (accountWhereClausesByMonth.size() > 0){
                def totalMonthlyAccountAmount = new Double(0)
                for (def monthlyWhereClause : accountWhereClausesByMonth){
                    def selectStatement = "from WellsFargoCheckingTransaction trans where (" + monthlyWhereClause.whereClause +
                            ") and (trans.transactionDate >= :startDate and trans.transactionDate < :endDate)"
                    def selectQuery = session.createQuery(selectStatement)
                    selectQuery = buildDateWhereClause(year, month, selectQuery)
                    def transactionList = selectQuery.list()
                    transactionList.each {
                        println it
                        totalMonthlyAccountAmount += it.amount * monthlyWhereClause.amountPercentage
                    }
                }

                session.save( new MonthlyAccountTotal(account: account, total: totalMonthlyAccountAmount, year: year, month: month))
                session.flush()
                session.clear()
            }

        }
        tx.commit()
    }

    private static def buildDateWhereClause(year, month, selectQuery){
        def endYear = year
        def endMonth = month + 1
        if (month == 12){
            endYear++
            endMonth = 1
        }

        def startDate = new SimpleDateFormat("yyyy/MM/dd").parse(year + "/" + month + "/01", new ParsePosition(0))
        def endDate = new SimpleDateFormat("yyyy/MM/dd").parse(endYear + "/" + endMonth + "/01", new ParsePosition(0))

        selectQuery.setParameter("startDate", startDate)
        selectQuery.setParameter("endDate", endDate)
        return selectQuery
    }
}
