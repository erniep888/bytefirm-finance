package com.bytefirm.finance

import com.bytefirm.finance.domain.Account

/**
 * Created by ernes_000 on 3/9/14.
 */
import java.io.File;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

class ExportToJson {

    static exportAccounts(factory) {
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def results = session.createQuery("from Account acct order by acct.displayOrder asc").list()
        saveResultsAsJson(results, "accounts.json")

        tx.commit()

    }

    static exportMonthlyAccountTotals(factory) {
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def results = session.createQuery("from MonthlyAccountTotal obj order by obj.year asc, obj.month asc, obj.account.displayOrder asc").list()
        saveResultsAsJson(results, "monthlyAccountTotals.json")

        tx.commit()

    }

    static exportMonthlyWhereClauses(factory) {
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def results = session.createQuery("from MonthlyWhereClause obj order by obj.year asc, obj.month asc, obj.account.displayOrder asc").list()
        saveResultsAsJson(results, "monthlyWhereClauses.json")

        tx.commit()

    }

    static exportWellsFargoCheckingTransactions(factory) {
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def results = session.createQuery("from WellsFargoCheckingTransaction obj order by obj.transactionDate asc").list()
        saveResultsAsJson(results, "wellsFargoCheckingTransactions.json")

        tx.commit()

    }

    static exportWhereClauseTemplates(factory){
        def session = factory.currentSession
        def tx = session.beginTransaction()

        def results = session.createQuery("from WhereClauseTemplate obj order by obj.account.displayOrder asc").list()
        saveResultsAsJson(results, "whereClauseTemplates.json")

        tx.commit()
    }

    private static saveResultsAsJson(results, outputFileName) {

        ObjectMapper mapper = new ObjectMapper();

        try {

            // convert user object to json string, and save to a file
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputFileName), results);

            // display to console
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results));

        } catch (JsonGenerationException e) {

            e.printStackTrace();

        } catch (JsonMappingException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }


}
