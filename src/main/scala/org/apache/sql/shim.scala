package org.apache.spark.sql
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

object Shim {
  def ofRows(session: SparkSession, plan: LogicalPlan): Dataset[Row] = {
    Dataset.ofRows(session, plan)
  }

  def stages(session: SparkSession): Seq[String] = {
    //session.sparkContext.statusStore.stageList()
    Seq.empty
  }


}


