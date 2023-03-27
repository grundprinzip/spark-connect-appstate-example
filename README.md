# spark-connect-appstate-example
An Example Plugin for Spark Connect


```build
mvn package
```

```shell
spark-shell --jars appstate-0.0.1.jar --conf spark.connect.extensions.relation.classes=de.grundprinzip.appstate.ConnectAppStateRelationPlugin --conf spark.sql.extensions=de.grundprinzip.appstate.AppStatusExtension
```

```python
from pyspark.sql.connect.session import SparkSession
import appstate

spark = SparkSession.builder.remote("sc://localhost").getOrCreate()
spark.sql("select count(*) from range(100)").collect()

spark.sqlExecutions().show()
```

```shell
+---+--------------------+-------------+--------------+--------------------+--------------------+
| id|         description|      started|completionTime|             details|             metrics|
+---+--------------------+-------------+--------------+--------------------+--------------------+
|  0|analyzePlan at Sp...|1679950061644|          null|org.apache.spark....|                  []|
|  1|executePlan at Sp...|1679950081439|          null|org.apache.spark....|[{avg hash probes...|
|  2|executePlan at Sp...|1679950082244|          null|org.apache.spark....|[{number of outpu...|
+---+--------------------+-------------+--------------+--------------------+--------------------+
```