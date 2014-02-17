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
import com.bytefirm.finance.domain.WhereClauseTemplate
import com.bytefirm.finance.domain.MonthlyWhereClause
import org.hibernate.Criteria
import org.hibernate.criterion.Restrictions
import org.hibernate.SessionFactory
import org.hibernate.Query


/**
 * Created by ernes_000 on 2/17/14.
 */
class WhereClauseTemplateBuilder {
    static buildFrom2013November(factory){
        def year = 2013
        def month = 11

        def session = factory.currentSession
        def tx = session.beginTransaction()

        def deleteQuery = session.createQuery("delete WhereClauseTemplate");
        int deleteResult = deleteQuery.executeUpdate();

        println 'Delete *************** ' + deleteResult

        def accounts = session.createQuery("from Account").list()

        for (Account account : accounts){
            def accountWhereClausesByMonth = session.createCriteria(MonthlyWhereClause.class)
                    .add(Restrictions.eq("account", account))
                    .add(Restrictions.eq("year", year))
                    .add(Restrictions.eq("month", month)).list()
            if (accountWhereClausesByMonth.size() > 0){
                def monthlyWhereClause = accountWhereClausesByMonth.get(0)
                session.save(new WhereClauseTemplate(account: monthlyWhereClause.account,
                        whereClause: monthlyWhereClause.whereClause, amountPercentage: monthlyWhereClause.amountPercentage ))
            }
        }
        tx.commit()
    }
}
