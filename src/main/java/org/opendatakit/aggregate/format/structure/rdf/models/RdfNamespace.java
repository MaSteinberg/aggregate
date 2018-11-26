package org.opendatakit.aggregate.format.structure.rdf.models;

public class RdfNamespace{
    public String prefix;
    public String uri;
    public RdfNamespace(String prefix, String uri){
        this.prefix = prefix;
        this.uri = uri;
    }
}
