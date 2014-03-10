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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.bytefirm.finance.domain.Account
import com.bytefirm.finance.domain.WellsFargoCheckingTransaction
import com.bytefirm.finance.domain.WhereClauseTemplate
import com.bytefirm.finance.domain.MonthlyWhereClause
import org.hibernate.cfg.*
import java.text.SimpleDateFormat
import java.text.ParsePosition

/**
 * Application launcher
 *
 * @author Ernie Paschall
 */
class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class)


    static main(args) {
        Application application = new Application()

        def configuration = application.configureHibernate()
        def factory = configuration.buildSessionFactory()

        // application.createWellsFargoChecking(factory)  // used to insert each month's csv  TODO: make this command driven

        def year = 2014
        def month = 2

        ExportToJson.exportAccounts(factory)
        ExportToJson.exportMonthlyAccountTotals(factory)
        ExportToJson.exportMonthlyWhereClauses(factory)
        ExportToJson.exportWellsFargoCheckingTransactions(factory)
        ExportToJson.exportWhereClauseTemplates(factory)

        // Only build from where clause template if you want to overwrite the existing month where clauses
//        for(int monthIndex = 2; monthIndex <= 12; monthIndex++){
//            MonthlyWhereClauseBuilder.buildFromWhereClauseTemplate(factory, year, monthIndex)
//        }





        // Displays the totals in the monthly account totals in short form
//        MonthlyAccountTool.displayMonthlyAccountTotal(factory, year, month)


        /***********************************************************************************/
        // Makes updates to the monthly account totals
//        MonthlyAccountTool.processMonthlyTransactions(factory, year, month)
        // Displays the transactions used to calculate the totals using the month's where clauses
//        MonthlyAccountTool.showProcessedTransactions(factory, year, month)
        // Displays accounted transaction total vs the raw transaction total...returns the difference
//        println MonthlyAccountTool.getBalanceDelta(factory, year, month)
        /**********************************************************************************/


        //MonthlyAccountTool.showAccountTotalsAsCsv(factory, year, month, year, month)

    }


    def createWellsFargoChecking(factory){
        def session = factory.currentSession
        def tx = session.beginTransaction()

//        def wellsFargoCsv = '/database/financeCsv/wellsfargo_checking_2012-11-01_to_2014_01__31.csv'
//        def wellsFargoCsv = '/database/financeCsv/wellsfargo_checking_2014_02.csv'
        def wellsFargoCsv = '/database/financeCsv/feb-2014.csv'

        BufferedReader bufferedReader = null
        String line = ""
        String cvsSplitBy = ","

        try{
            SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy")
            double total = 0
            bufferedReader = new BufferedReader(new FileReader(wellsFargoCsv))
            while ((line = bufferedReader.readLine()) != null){
                line = line.replaceAll("\"", "");
                String[] columns = line.split(cvsSplitBy)
                if (columns.length > 4){
                    WellsFargoCheckingTransaction checkingTransaction = new WellsFargoCheckingTransaction(
                            transactionDate: dateFormatter.parse(columns[0], new ParsePosition(0)), amount: new Double(columns[1]),
                            description: columns[4])
                    session.save(checkingTransaction)
                }
                else
                    println 'no more lines? The number of columns is: ' + columns.length
            }

        } catch (FileNotFoundException fnfe){
            fnfe.printStackTrace()
        } catch (IOException ioe) {
            ioe.printStackTrace()
        } finally {
            if (bufferedReader != null){
                try{
                    bufferedReader.close()
                } catch (IOException ioe2){
                    ioe2.printStackTrace()
                }
            }
        }

        tx.commit()
    }

    def createAccounts(factory) {
        def session = factory.currentSession
        def tx = session.beginTransaction()

        session.save(new Account(title:'Byte Firm Sales', type: 'Income', description: 'Product and Services Sales', displayOrder: 10))
        session.save(new Account(title:'Byte Firm Consulting', type: 'Income', description: 'Revenue from Consulting', displayOrder: 20))
        session.save(new Account(title:'Other Income', type: 'Income', description: 'Revenue from other sources.', displayOrder: 30))

        session.save(new Account(title:'Rent', type: 'Expense', description: 'Office rent', displayOrder: 40))
        session.save(new Account(title:'Utilities', type: 'Expense', description: 'Cellphones, power, water, trash, etc.', displayOrder: 50))
        session.save(new Account(title:'Petty Cash', type: 'Expense', description: 'Random expenses including cash withdrawals', displayOrder: 60))
        tx.commit()
    }


    def configureHibernate() {
        def props = [
        ]

        def config = new AnnotationConfiguration()

        config.addAnnotatedClass(Account)
        config.addAnnotatedClass(WellsFargoCheckingTransaction)
        config.addAnnotatedClass(WhereClauseTemplate)
        config.addAnnotatedClass(MonthlyWhereClause)
        config.addAnnotatedClass(MonthlyAccountTotal)
        return config
    }
}
