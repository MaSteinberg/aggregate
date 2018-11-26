package org.opendatakit.aggregate.format.structure.rdf.models;

import java.util.List;

public class NamespacesModel{
    public String base;
    public List<RdfNamespace> namespaces;

    public NamespacesModel(String base){
        this.base = base;
    }

    public NamespacesModel(String base, List<RdfNamespace> namespaces){
        this.base = base;
        this.namespaces = namespaces;
    }
}

