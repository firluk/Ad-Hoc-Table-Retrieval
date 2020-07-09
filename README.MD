<!--
    Comments
 -->

Ad-Hoc Table Retrieval
====================



Abstract
---------------------

The focus of this project is the “Ad-hoc Table Retrieval” task. Within such a setting,
a user may post questions in natural language and answers are tables that contain
relevant information.

Dataset
---------------------

The official dataset of this project is the WikiTables collection which can be
downloaded from: 

https://github.com/iai-group/www2018-table (Test collection)

### Additional datasets
- [enwiki-20141208-pages-articles-multistream.xml.bz2](https://archive.org/download/enwiki-20141208/enwiki-20141208-pages-articles-multistream.xml.bz2)  
    
    Wikipedia database dump - Used for approximating extrinsic similarity (table to page)

-  [word2vec-GoogleNews-vectors](https://drive.google.com/file/d/0B7XkCwpI5KDYNlNUTTlSS21pQmM/edit)
    
    Word2Vec - Used for calculating cosine similarity
    
### Libraries and Frameworks
- [lucene](https://lucene.apache.org/core/)
- [org.json](https://github.com/stleary/JSON-java)
- [DL4J](https://github.com/eclipse/deeplearning4j)
- [nd4j](https://github.com/deeplearning4j/nd4j)
- [wikixmlj](https://github.com/delip/wikixmlj) 

Evaluation
---------------------

The question set to be evaluated in the official one for this task (having 60
queries in total) which is found in https://github.com/iai-group/www2018-table/blob/master/data/queries.txt.

The set of qrels for this task can be also found in: https://github.com/iai-group/www2018-table/blob/master/data/qrels.txt

Project Structure
---------------------
```$xslt
|   
+---libs
|   \--- ... local maven repository containing `wikixmlj` jar
+---src
|   +---main
|   |   +---java
|   |   |
|   |   |  ... java source code ...
|   |   |           
|   |   \---resources
|   |           config.properties 
|   |           [all the properties for the project]
|   |           log4j2.xml 
|   |           [logger definition]

```


Build
---------------------
Run from the root project folder containing pom.xml file `mvn package`
 
Per completion the ./target/ folder will hold
```$xslt
|
|   config.properties
|   [config.properties which resided in ./resources]
|   ir.tableir-*.*.*.jar
|   [executable jar]
|   
+---libs
|   [all of the jar dependencies, including `wikixmlj` and all the others]
+--- ... other directories ...

```
 
Usage
---------------------
When running the _jar_ file via `java -jar ir.tableir-*.*.*.jar` the executable will look for
`config.properties` file in the working directory  (.\\), otherwise it will utilize `config.properties`
bundled in the _jar_ file.

### config.properties
```$xslt
#Dataset and Index Location Properties
# if this file is present in the same directory as executing the index/search
# jar, then this file is read, even if there is one in resources folder 
# several assumptions are made: 
# all files are under the same mount
# work directory is where all the directories for index and other data
# reside
# for example:
# J:\\Haifa University\\2019-2020\\Information Retrieval\\Dataset\\queries.txt
# is the full path to the queries file
mount=J:\\
work_directory=Haifa University\\2019-2020\\Information Retrieval\\

queries=Dataset\\queries.txt

# these folders are generated if they do not exist
tables_index_directory=Project\\tables_index\\
enwiki_index_directory=Project\\wiki_index\\

# json tables dataset
json_table_directory=Dataset\\tables_redi2_1\\
# wikipedia dump
enwiki=Dataset\\enwiki-20141208-pages-articles-multistream.xml
# word2vec model
word_vectors_model_file=Dataset\\GoogleNews-vectors-negative300.bin.gz

# the directory where the trecreport will reside
trec_output_directory=Project\\TRECReports

#Index and retrieve strategy
table_strategy=singleField
```

List of command-line options
---------------------
~~~
usage: java -jar ir.tableir-*.*.*.jar [-p]|[-w]|[-i]|[-s="search query"]|[-q="number for query from queries.txt"]|[-a]] 
-w, --wiki              index the wiki xml dump
-i, --index             index the directory with tables jsons
-p, --properties_file   explicit properties file
-s, --search            search the index for query
-q, --query_number      search the index for query with given index from queries file)
-a, --all_queries       perform search on all queries in queries file (defined by properties)
                        after successful run of all_queries the output file
                        will be generated under 
~~~
