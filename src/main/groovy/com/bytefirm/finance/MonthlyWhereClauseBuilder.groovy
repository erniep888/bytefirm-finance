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

import com.bytefirm.finance.domain.Account
import com.bytefirm.finance.domain.MonthlyWhereClause
import com.bytefirm.finance.domain.WhereClauseTemplate
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.hibernate.SessionFactory
import org.hibernate.Query

/**
 * Created by ernes_000 on 2/17/14.
 */
class MonthlyWhereClauseBuilder {

    static buildFromWhereClauseTemplate(factory, year, month){
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def deleteQuery = session.createQuery("delete MonthlyWhereClause where year = :year and month = :month");
        deleteQuery.setParameter("year", year);
        deleteQuery.setParameter("month", month);
        int deleteResult = deleteQuery.executeUpdate();

        println 'Delete *************** ' + deleteResult

        def accounts = session.createQuery("from Account").list()

        for (Account account : accounts){
            def accountWhereClauseTemplates = session.createCriteria(WhereClauseTemplate.class)
                    .add(Restrictions.eq("account", account)).list()
            if (accountWhereClauseTemplates.size() > 0){
                def whereClauseTemplate = accountWhereClauseTemplates.get(0)
                session.save(new MonthlyWhereClause(account: whereClauseTemplate.account,
                        whereClause: whereClauseTemplate.whereClause, amountPercentage: whereClauseTemplate.amountPercentage,
                        year: year, month: month))
            }
        }
        tx.commit()
    }


}
