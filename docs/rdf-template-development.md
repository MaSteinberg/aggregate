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

#### namespaces.ttl.mustache
This file is responsible for the namespace and prefix definitions.
The Model for this template provides access to the following fields:
+ *base* - **String** - Base URI of the resulting RDF file
+ *namespaces* - **List** of namespaces that the form-designer added, each consisting of:
  + *prefix* - **String** - Prefix for this namespace
  + *uri* - **String** - URI of this namespace

The recommendation for this file is to include the following code:
```
@base <{{base}}> .
@prefix : <{{base}}> .
{{#namespaces}}
    @prefix {{prefix}}: <{{uri}}> .
{{/namespaces}}
```
This snippet will define *base* as the base URI and as the empty prefix and define all other namespaces with their respective prefixes.

All namespaces that are used in your templates should be added manually below the mentioned code-snippet in the following form:
```
@prefix ex: <http://example.org/> .
```
**Do not rely on the form-designer to add the namespaces that you need!**
The form-designer is only responsible for adding **additional** namespaces for semantic information that he provides during form design.

#### toplevel.ttl.mustache
This file is responsible for defining any RDF statements on the top level of the exportable dataset.
The template is rendered exactly once for a given export process.
The model provides access to metadata about the form and the dataset.
The following fields are exposed:
+ *toplevelEntityIdentifier* - **String** - Can be used as a subject, predicate or object in an RDF statement to uniquely identify the top level of the resulting RDF file.
+ *formId* - **String** - Unique identifier of the form used for data collection.
+ *formName* - **String** - Name of the form used for data collection. Not necessarily unique. Might be empty.
+ *formDescription* - **String** - Description of the form used for data collection. Might be empty.
+ *formCreationDate* - **String** - DateTime of the form's creation. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formCreationUser* - **String** - Name of the user who designed the form.
+ *lastUpdate* - **String** - DateTime of the form's latest update. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formVersion* - **String** - Name of the form's latest version. Might be empty.

This template file would usually contain triples that define one or more resources that the other template files can reference.
The fields exposed by the model can then be attached to these resources.
Alternatively the template file may be left empty if there is no need for such top level resources.

#### row.ttl.mustache
This file is responsible for defining RDF statements on the row level of the exportable dataset.
The template is rendered exactly once for each submission included in the export.
The model provides access to metadata about the current row.
The following fields are exposed:
+ *topLevelModel* - Same model as in toplevel.ttl.mustache - Contains metadata about the form and the dataset.
+ *rowEntityIdentifier* - **String** - Can be used as a subject, predicate or object in an RDF statement to uniquely identify the current submission.
+ *rowId* - **String** - Unique identifier of the current submission. Depending on the selection during the start of the export this is either a whole number (starting at 1, incrementing for each row) or a globally unique identifier (UUID), e.g. `uuid:ace1da01-038f-40af-a1fb-d6758d052c36`.

This template file would usually contain triples that define one or more resources per row that the other template files can reference.
Alternatively the template file may be left empty if there is no need for such row level resources.