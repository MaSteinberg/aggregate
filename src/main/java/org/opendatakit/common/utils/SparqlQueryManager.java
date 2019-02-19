package org.opendatakit.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.opendatakit.aggregate.client.form.SemanticAutocompleteElement;

import java.util.ArrayList;
import java.util.List;

public class SparqlQueryManager {
    /*
    Issues the given query on the given SPARQL-Endpoint.
    Returns a list of SemanticAutocompleteElements,
    using ?uri from the query as the uri and ?displayName as the display name
     */
    public static List<SemanticAutocompleteElement> issueAutocompletionSparqlQuery(String endpoint, String query){
        List<SemanticAutocompleteElement> results = new ArrayList<>();
        //Connect to remote SPARQL-endpoint
        Repository repo = new SPARQLRepository(endpoint);
        repo.initialize();
        try(RepositoryConnection conn = repo.getConnection()){
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            try(TupleQueryResult result = tupleQuery.evaluate()){
                //Iterate results
                while(result.hasNext()){
                    BindingSet bindingSet = result.next();
                    //Check if ?uri has been bound in the result
                    if(!bindingSet.hasBinding("uri")){
                        throw new IllegalArgumentException("?uri doesn't seem to be part of the issued SPARQL-query!");
                    }
                    //Default to an empty display name if it's not part of the query
                    String displayName = bindingSet.hasBinding("displayName")
                            ? bindingSet.getValue("displayName").stringValue()
                            : "";
                    String uri = bindingSet.getValue("uri").stringValue();
                    //Check that bound ?uri value is not blank
                    if(StringUtils.isBlank(uri)){
                        throw new IllegalArgumentException("?uri returned empty!");
                    }
                    //Add query-result to our result-list
                    results.add(new SemanticAutocompleteElement(uri, displayName));
                }
            }
        }
        return results;
    }
}
