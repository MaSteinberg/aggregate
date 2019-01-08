# RDF Template Development

## Introduction
This guide documents the steps necessary to set up a new RDF template group for the RDF export.
This setup includes the registration and configuration of the template group and the construction of all necessary template files.
It also described hints and best practices to ensure that the resulting file complies with the [RDF/Turtle syntax](https://www.w3.org/TR/turtle/).

### Registration and configuration
RDF template groups are registered in the rdfExportTemplateConfig.yml file located in `<your-project-root>/src/main/resources/rdfExport`.
The file is written in [YAML](https://yaml.org/spec/1.2/spec.html)-Syntax.
Please keep in mind that this syntax is indentation-sensitive and is using spaces for indentation.
Furthermore, the convention for this configuration-file is to use [camel case](https://en.wikipedia.org/wiki/Camel_case) if any name consists of more than one word.

The first list in this file defines the semantic information, called metrics, that the template groups can make use of.
If your new template requires some information that is not yet listed, this is the first place to add it.
Make sure to choose a name that is as descriptive as possible.

You can register your new template group by adding its name to the "templates" node.
The template group itself is a node that has to contain two lists: *optionalMetrics* and *requiredMetrics*.
They should contain the metrics that your template group can process.
The *requiredMetrics* list should contain the metrics that you deem indispensible for your template, i.e. your template would either not produce valid RDF if this information was missing or the resulting RDF would not make sense.
The *optionalMetrics* list should then contain the other metrics that your template can process to enrichen the resulting RDF.
**Both lists have to be added, even if they are empty.**

### Necessary files
There are multiple directories and files that have to be added to the project in order to get your new template group up and running.
The files are used as input for the [Mustache.java](https://github.com/spullara/mustache.java)-library.
Therefore they have to comply with the syntax that is expected by the library.
The following structure has to be added to the `<your-project-root>/src/main/resources/rdfExport/mustache_templates` directory of the project (directories are displayed as **bold**, files as *italic* ):

+ **\<yourTemplateName>**
  + **elementTypeCells**
    + *booleanCell.ttl.mustache*
    + *dateCell.ttl.mustache*
    + *dateTimeCell.ttl.mustache*
    + *decimalCell.ttl.mustache*
    + *geolocationCell.ttl.mustache*
    + *geoshapeCell.ttl.mustache*
    + *geotraceCell.ttl.mustache*
    + *integerCell.ttl.mustache*
    + *multipleChoiceCell.ttl.mustache*
    + *select1Cell.ttl.mustache*
    + *stringCell.ttl.mustache*
    + *timeCell.ttl.mustache*
  + *cell.ttl.mustache*
  + *column.ttl.mustache*
  + *namespaces.ttl.mustache*
  + *row.ttl.mustache*
  + *toplevel.ttl.mustache*

Additionally, you might notice four files located in `<your-project-root>/src/main/resources/rdfExport/mustache_templates/common`:

+ **common**
  + *cellIdentifier.mustache*
  + *columnIdentifier.mustache*
  + *rowIdentifier.mustache*
  + *toplevelIdentifier.mustache*

These files are used by all of the RDF template groups and should not be modified.
The following sections explain the purpose of the files you have to build and the data that can be accessed in each of them.
**General advice**: Use one of the existing template groups as a guideline for the structure and content of the files.
**Terminology**: In the following sections, the object which contains the data that is accessible in the different template-files will be called **model** (in technical terms, this is the Java-object that is passed to the Mustache-renderer).

