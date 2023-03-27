package de.grundprinzip.appstate

import com.google.protobuf
import de.grundprinzip.appstate.proto.AppStateRelation

import org.apache.spark.sql.{Row, SparkSession, SparkSessionExtensions}
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.{LeafCommand, LogicalPlan}
import org.apache.spark.sql.connect.planner.SparkConnectPlanner
import org.apache.spark.sql.connect.plugin.RelationPlugin
import org.apache.spark.sql.execution.{LeafExecNode, SparkPlan, SparkStrategy}
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec
import org.apache.spark.sql.types.{ArrayType, DateType, LongType, StringType, StructField, StructType}

/**
 * Simple command to list all SQL Executions åccessible from the current Spark Session.
 * @param output
 */
case class ShowSqlExecutions(
    override val output: Seq[Attribute] = ShowSqlExecutions.getOutputAttrs)
    extends LeafCommand {}

/**
 * Helper object to build the schema of the output.
 */
object ShowSqlExecutions {
  def getOutputAttrs: Seq[Attribute] = Seq(
    AttributeReference("id", LongType, nullable = false)(),
    AttributeReference("description", StringType, nullable = false)(),
    AttributeReference("started", LongType, nullable = false)(),
    AttributeReference("completionTime", DateType, nullable = true)(),
    AttributeReference("details", StringType, nullable = false)(),
    AttributeReference(
      "metrics",
      ArrayType(
        StructType(
          Seq(
            StructField("name", StringType, false),
            StructField("metricType", StringType, false),
            StructField("value", StringType, true)))))())
}

/**
 * Execution node for accessing the data.
 * @param output
 */
case class ShowSqlExecutionsExec(output: Seq[Attribute]) extends V2CommandExec with LeafExecNode {
  override protected def run(): Seq[InternalRow] = {
    val sharedState = SparkSession.getActiveSession.get.sharedState
    val executions = sharedState.statusStore.executionsList()
    executions.map { e =>
      val metrics = e.metrics.map(x =>
        Row(x.name, x.metricType, e.metricValues.getOrElse(x.accumulatorId, "N/A")))
      toCatalystRow(e.executionId, e.description, e.submissionTime, null, e.details, metrics)
    }
  }
}

class AppStatusStrategy extends SparkStrategy {
  override def apply(plan: LogicalPlan): Seq[SparkPlan] = {
    plan match {
      case sj: ShowSqlExecutions => Seq(ShowSqlExecutionsExec(sj.output))
      case _ => Nil
    }
  }
}

class AppStatusExtension extends Function1[SparkSessionExtensions, Unit] {
  override def apply(ext: SparkSessionExtensions): Unit = {
    ext.injectPlannerStrategy { _ =>
      new AppStatusStrategy()
    }
  }
}

/**
 * Plugin
 */
class ConnectAppStateRelationPlugin extends RelationPlugin {
  override def transform(
      relation: protobuf.Any,
      planner: SparkConnectPlanner): Option[LogicalPlan] = {
        if (!relation.is(classOf[AppStateRelation])) {
          None
        } else {
          Some(ShowSqlExecutions())
        }
  }
}
