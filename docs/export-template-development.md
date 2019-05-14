# Export Template Development

## TOC
- [Export Template Development](#export-template-development)
  - [TOC](#toc)
  - [Introduction](#introduction)
  - [Registration and configuration](#registration-and-configuration)
    - [Identifier](#identifier)
    - [Endpoint](#endpoint)
    - [Query](#query)
  - [Necessary files](#necessary-files)
    - [config.yml](#configyml)
    - [namespaces.mustache](#namespacesmustache)
    - [toplevel.mustache](#toplevelmustache)
    - [row.mustache](#rowmustache)
    - [column.mustache](#columnmustache)
    - [cell.mustache](#cellmustache)
    - [elementTypeCells](#elementtypecells)
      - [booleanCell.mustache, decimalCell.mustache, integerCell.mustache, singleChoiceCell.mustache, stringCell.mustache](#booleancellmustache-decimalcellmustache-integercellmustache-singlechoicecellmustache-stringcellmustache)
      - [multipleChoiceCell.mustache](#multiplechoicecellmustache)
      - [dateCell.mustache, timeCell.mustache, dateTimeCell.mustache](#datecellmustache-timecellmustache-datetimecellmustache)
      - [geolocationCell.mustache](#geolocationcellmustache)
      - [geotraceCell.mustache, geoshapeCell.mustache](#geotracecellmustache-geoshapecellmustache)
    - [termination.mustache](#terminationmustache)

## Introduction
This guide documents the steps necessary to set up a new export template group for the flexible, template-based export.
This setup includes the registration and configuration of the template group and the construction of all necessary template files.

## Registration and configuration
Export template groups are registered in the *TemplateExportConfig.yml*-file located in `<your-project-root>/src/main/resources/templateExport`.
The file is written in [YAML](https://yaml.org/spec/1.2/spec.html "Yaml Specification")-Syntax.
Please keep in mind that in many cases this syntax is indentation-sensitive and is using spaces for indentation.
Furthermore, the convention for this configuration-file is to use [camel case](https://en.wikipedia.org/wiki/Camel_case "Wikipedia: Camel Case") if any name consists of more than one word.

The first list in this file, `availableProperties`, defines the semantic properties that the template groups can make use of.
If your new template requires some information that is not yet listed, this is the first place to add it.
Make sure to choose a name that is unique and as descriptive as possible.
Please do not edit the existing properties unless you are absolutely sure that none of the currently uploaded forms on the Aggregate server is using it.
Each semantic property consists of three types of information: The identifier of the property, a [SPARQL](https://www.w3.org/TR/sparql11-overview/ "SPARQL Specification") endpoint and a SPARQL query.

### Identifier
A unique identifier of the semantic property that template groups can refer to.
### Endpoint
An [RDF4J](http://rdf4j.org/ "RDF4J Website") compatible SPARQL-endpoint that contains a vocabulary that should be used for the semantic property.
### Query
A SPARQL-query that is used to query the endpoint and get the available values for the semantic property.
The query has to use a variable called `?uri` to bind the URIs of the queried terms and `?displayName` to bind labels for the queried terms (e.g. attached via rdfs:label, skos:prefLabel, ...).

Given an endpoint and a query, an autocompletion will be provided to survey authors who want to use the semantic property to annotate their surveys.
If *Endpoint* and *Query* are intentionally left empty, no autocompletion will be provided.

A new template group can then be registered by simply adding it to the *templates* list at the end of the configuration file.
Further configuration, e.g. which semantic properties it uses, will be added in a separate file.

The following snippet shows a valid *TemplateExportConfig.yml*-file:

```
availableProperties:
  Creator:
    Endpoint:
    Query:
  Unit:
    Endpoint: http://192.168.0.8:7200/repositories/om
    Query: |-
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      PREFIX : <http://ecoinformatics.org/oboe/oboe.1.2/oboe.owl#>
      PREFIX om: <http://www.ontology-of-units-of-measure.org/resource/om-2/>
      PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

      SELECT DISTINCT ?uri ?displayName
      WHERE {
          ?uri rdf:type om:Unit .
          OPTIONAL{
              ?uri rdfs:label ?displayName .
           	FILTER (lang(?displayName) = 'en')
          }
      }
  Characteristic:
    Endpoint: http://192.168.0.8:7200/repositories/oboe
    Query: |-
      PREFIX oboe-core: <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#>
      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
      PREFIX : <http://ecoinformatics.org/oboe/oboe.1.2/oboe.owl#>
      SELECT DISTINCT ?uri ?displayName
      WHERE {
      	?uri rdfs:subClassOf oboe-core:Characteristic .
          FILTER NOT EXISTS {
              ?sub rdfs:subClassOf ?uri .
          }
          OPTIONAL{
              ?uri rdfs:label ?displayName
          }
      }
templates:
  - oboe
```

## Necessary files
There are multiple directories and files that have to be added to the project in order to get your new template group up and running.
The first directory that has to be created under `<your-project-root>/src/main/resources/templateExport/mustache_templates` is simply a directory named after your new template group - for above *TemplateExportConfig.yml*-file that would mean a directory called `oboe`.

### config.yml
The first file that should be created in this new directory is the *config.yml*-file.
This file describes the configuration of your new template group.
It contains a *displayName* property that defines under which name the template group should be presented to people who use the template-based export.
It can also contain a *filetype* property that should be set to the file-extension of the resulting export file, e.g. *.ttl* for [RDF/Turtle syntax](https://www.w3.org/TR/turtle/ "RDF/Turtle Specification") or *.xml* for XML based files.
Then it defines the *templateProperties* with two sub-keys: *optionalProperties* and *requiredProperties*.
These should contain the semantic properties that your template group can process.
The *requiredProperties* list should contain the properties that you deem indispensible for your template, i.e. your template would either not produce a valid output if this information was missing or the resulting file would not make sense.
The *optionalProperties* list should then contain the other properties that your template can process to enrich the resulting output file.

The following snippet shows a valid *config.yml* file for the `oboe`-template:

```
displayName: Extensible Observation Ontology
filetype: .ttl
templateProperties:
  optionalProperties:
    - Creator
  requiredProperties:
    - Characteristic
    - Unit
```

Once the `config.yml` is defined, a few files have to be defined to describe how the export result will be built during the export.
The files we build are used as input for the [Mustache.java](https://github.com/spullara/mustache.java "Mustache-Java Website")-library.
Therefore they have to comply with the syntax that is expected by the library.
The following structure has to be added to the directory of the project (directories are displayed as **bold**, files as *italic* ):

+ **\<yourTemplateName>**
  + *config.yml*
  + *cell.mustache*
  + *column.mustache*
  + *namespaces.mustache*
  + *row.mustache*
  + *termination.mustache*
  + *toplevel.mustache*
  + **elementTypeCells**
    + *booleanCell.mustache*
    + *dateCell.mustache*
    + *dateTimeCell.mustache*
    + *decimalCell.mustache*
    + *geolocationCell.mustache*
    + *geoshapeCell.mustache*
    + *geotraceCell.mustache*
    + *integerCell.mustache*
    + *multipleChoiceCell.mustache*
    + *singleChoiceCell.mustache*
    + *stringCell.mustache*
    + *timeCell.mustache*

Additionally, you might notice four files located in `<your-project-root>/src/main/resources/templateExport/mustache_templates/common`:

+ **common**
  + *cellIdentifier.mustache*
  + *columnIdentifier.mustache*
  + *rowIdentifier.mustache*
  + *toplevelIdentifier.mustache*

These files are used by all of the template groups and **should not be modified**.

The following sections explain the purpose of the files you have to build and the data that can be accessed in each of them.

**General advice**: Use one of the existing template groups as a guideline for the structure and content of the files.

**Terminology**: In the following sections, the object which contains the data that is accessible in the different template-files will be called **Model** (in technical terms, this is the Java-object that is passed to the Mustache-engine).

**Examples**: The example-snippets shown in the folowing sections are based on RDF/Turtle syntax. Templates for alternative formats will look severely different.

**Order of Execution**: When creating templates for structures in which the order of statements and/or the nesting of elements is important, the order in which the templates are being executed is vital.
The templates will be executed in the following order:

1. namespaces.mustache
2. toplevel.mustache
3. For each column/question: column.mustache
4. For each row/submission:
   1. row.mustache
   2. For each column/question: 
      1. cell.mustache
      2. datatype-specific cell template
5. termination.mustache

### namespaces.mustache
This file is responsible for namespace and prefix definitions.
The Model for this template provides access to the following field:
+ *.* - **String** - Base URI of the resulting file

When building RDF/Turtle, the recommendation for this file is to include the following code:
```
@base <{{.}}> .
@prefix : <{{.}}> .
```
This snippet will define the base URI and the empty prefix.

All namespaces that are used in your templates should then be added manually below the mentioned code-snippet in the following form:
```
@prefix ex: <http://example.org/> .
```

### toplevel.mustache
This template is responsible for defining any statements at the top of the resulting exported file.
The template is rendered exactly once for a given export process.
The Model provides access to metadata about the form and the dataset.
The following fields are exposed:
+ *toplevelIdentifier* - **String** - Unique identifier for the survey.
+ *formId* - **String** - Unique identifier of the form used for data collection.
+ *formName* - **String** - Name of the form used for data collection. Not necessarily unique. Might be empty.
+ *formDescription* - **String** - Description of the form used for data collection. Might be empty.
+ *formCreationDate* - **String** - DateTime of the form's creation. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formCreationUser* - **String** - Name of the user who designed the form.
+ *lastUpdate* - **String** - DateTime of the form's latest update. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formVersion* - **String** - Name of the form's latest version. Might be empty.

In RDF/Turtle templates, this template file would usually contain triples that define one or more resources that the other template files can reference.
The fields exposed by the Model can then be attached to these resources.
Alternatively the template file may be left empty if there is no need for such top level resources.

### row.mustache
This file is responsible for defining statements on the row level of the exportable dataset.
The template is rendered exactly once for each submission included in the export.
The Model provides access to metadata about the current row.
It also includes the toplevel-Model in case any toplevel information should be processed at the row level.
The following fields are exposed:
+ *topLevelModel* - Same Model as in toplevel.mustache - Contains metadata about the form and the dataset.
+ *rowId* - **String** - Unique identifier of the current submission. Depending on the selection during the start of the export this is either a whole number (starting at 1, incrementing for each row) or a globally unique identifier (UUID), e.g. `uuid:ace1da01-038f-40af-a1fb-d6758d052c36`.
+ *rowIdentifier* - **String** - Unique identifier for the row. Similar to *rowId*: Depending on the selection during the start of the export this is either globally or locally unique.
+ *isFirstRow* - **Boolean** - Flag that signals, whether the row for which the template is currently being executed is the first row to be exported.

In RDF/Turtle template, this template file would usually contain triples that define one or more resources per row that the other template files can reference.
Alternatively the template file may be left empty if there is no need for such row level resources.

### column.mustache
This file is responsible for defining statements on the column level of the exportable dataset.
The template is rendered exactly once for each column included in the export (i.e. field collected by the survey).
The Model provides access to metadata about the current column.
It also includes the toplevel-Model in case any toplevel information should be processed at the column level.
The following fields are exposed:
+ *topLevelModel* - Same Model as in toplevel.mustache - Contains metadata about the form and the dataset.
+ *columnHeader* - **String** - Unique name of the current column. This might differ from the **label** displayed during the survey.
+ *columnIdentifier* - **String** - Unique identifier for the current column.
+ *isFirstColumn* - **Boolean** - Flag that signals, whether the column for which the template is currently being executed is the first column to be exported.
+ *isLastColumn* - **Boolean** - Flag that signals, whether the column for which the template is currently being executed is the last column to be exported.

In RDF/Turtle templates, this template file would usually contain triples that define one or more resources per column that the other template files can reference.
Alternatively the template file may be left empty if there is no need for such column level resources.

### cell.mustache
This file is the first template that is rendered for each of the data elements of the exportable dataset.
It is rendered exactly once for each cell (i.e. data element), **regardless of the datatype**.
Thus the Model provides access to metadata of the cell that is independent from the datatype.
It also includes both the column- and row-Models in case any of the information contained in those should be processed on the cell level.
Remember that both the column- and row-Models also allow access to the toplevel-Model.
The following fields are exposed:
+ *rowModel* - Same Model as in row.mustache - Contains metadata about the row that contains the current cell.
+ *columnModel* - Same Model as in column.mustache - Contains metadata about the column that contains the current cell.
+ *cellIdentifier* - **String** - Unique identifier for the current cell.
+ *semantics* - **Map\<String, SemanticsModel>** - This map contains the properties that you configured in the `TemplateExportTemplateConfig.yml`-file. The SemanticsModel for a given semantic property can be accessed with `semantics.YourPropertyName`. The SemanticsModel exposes the following fields:
  + *value* - **String** - The value of the property.
  + *isLiteral* - **Boolean** - Flag signalling whether the *value* should be considered a literal or a URI. Typically the Model could be used in the following way: 
  ```
    1 {{#semantics.YourPropertyName.isLiteral}}
    2   {{cellIdentifier}} ex:somePredicate "{{semantics.YourPropertyName.value}}" .
    3 {{/semantics.YourPropertyName.isLiteral}}
    4 {{^semantics.YourPropertyName.isLiteral}}
    5   {{cellIdentifier}} ex:somePredicate <{{semantics.YourPropertyName.value}}> .
    6 {{/semantics.YourPropertyName.isLiteral}}
  ```
  This snippet would render line 2 if the value is considered a literal or line 5 if the value is considered an RDF resource.
  In this example the only difference are the quotation marks surrounding the literal value and accordingly the angle brackets for the not-literal value but of course more complex distinctions, e.g. using different predicates, are possible.

  Please be aware that if you marked a property as **optional** the *semantics* map might not contain your property.
  To make sure no incomplete statements are produced you can use the following snippet:
  ```
    {{#semantics.YourPropertyName}}
    The code in here is only executed if the map contains YourPropertyName.
    {{/semantics.YourPropertyName}}
  ```
  As always, feel free to check out the existing templates if anything seems unclear.

### elementTypeCells
The *elementTypeCells*-folder contains files for all different data types that can be collected by the survey.
For RDF/Turtle templates, the recommendation for these files is to attach the value(s) of the cell to the RDF-resources you defined in the other template files, using predicates and XSD-Datatypes in a way that is appropriate for the cell's datatype.
The different files have different Models, described in the following sections.
Please note that all of these Models extend the basic cell Model used in *cell.mustache*, so all fields available in the basic Model, including the semantics Map, are also available in the datatype-specific files.

#### booleanCell.mustache, decimalCell.mustache, integerCell.mustache, singleChoiceCell.mustache, stringCell.mustache
The Models in these files expose the following fields:
+ *cellValue* - **String** - the collected value of the cell.

#### multipleChoiceCell.mustache
The Model in this file exposes the following fields:
+ *cellValues* - **List\<String>** - List of the selected values.

You can iterate through the list using a snippet like the following:
```
{{#cellValues}}
  "{{.}}"^^xsd:string
{{/cellValues}}
```

#### dateCell.mustache, timeCell.mustache, dateTimeCell.mustache
The Models in these files expose the following fields:
+ *date* - **String** - Date that was collected.
+ *time* - **String** - Time that was collected.

While both fields are available in all three files, please note that *time* might not contain a meaningful or even valid date in *dateCell.mustache* and accordingly for *date* in *timeCell.mustache*.

#### geolocationCell.mustache
The Model in this file exposes the following fields:
+ *location* - **Location** - The location that was collected. This object in turn exposes the fields:
  + *latitude* - **String** - The latitude of the collected location.
  + *longitude* - **String** - The longitude of the collected location.
  + *altitude* - **String** - The altitude of the collected location.
  + *accuracy* - **String** - The accuracy of the collected location.

#### geotraceCell.mustache, geoshapeCell.mustache
The Models in these files expose the following fields:
+ *locationList* - **List\<GeotraceElement>** - List of the collected locations. Each element exposes the fields:
  + *pathElementIndex* - **Integer** - Ordinal number of this location, starting at 1.
  + *location* - **Location** - The collected location. For the fields that this object exposes, please refer to the [geolocationCell.mustache](#geolocationcellmustache) section.

### termination.mustache
This file is responsible for defining statements at the very end of the resulting exported file.
The template is rendered exactly once for a given export process.
The Model is the same as in the toplevel.mustache template, providing access to the following fields:
+ *toplevelIdentifier* - **String** - Unique identifier for the survey.
+ *formId* - **String** - Unique identifier of the form used for data collection.
+ *formName* - **String** - Name of the form used for data collection. Not necessarily unique. Might be empty.
+ *formDescription* - **String** - Description of the form used for data collection. Might be empty.
+ *formCreationDate* - **String** - DateTime of the form's creation. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formCreationUser* - **String** - Name of the user who designed the form.
+ *lastUpdate* - **String** - DateTime of the form's latest update. Format: yyyy-MM-dd HH:mm:ss.fff
+ *formVersion* - **String** - Name of the form's latest version. Might be empty.

This template can be left empty for order-agnostic formats like RDF/Turtle, because statements about the survey could simply be added in the toplevel.mustache template.
For other formats, the termination template can be very important.
For example, in XML templates it can be used to close tags that were opened in the toplevel.mustache template.