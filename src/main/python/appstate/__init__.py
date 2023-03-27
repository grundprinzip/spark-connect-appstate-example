from typing import Optional
from pyspark.sql.connect.plan import LogicalPlan
import pyspark.sql.connect.proto as proto
from pyspark.sql.connect.session import SparkSession
from pyspark.sql.connect.dataframe import DataFrame

from .plugin_pb2 import AppStateRelation
from google.protobuf.any_pb2 import Any



class AppState(LogicalPlan):

    def __init__(self) -> None:
        super().__init__(None)

    def plan(self, session: "SparkConnectClient") -> proto.Relation:
        plan = proto.Relation()
        plan.extension.Pack(AppStateRelation())
        return plan



# Monkey Patching
def sqlExecutions(self: SparkSession):
    return DataFrame.withPlan(AppState(), self)


SparkSession.sqlExecutions = sqlExecutions