/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.spark.testsuite.nullvalueserialization

import org.apache.spark.sql.common.util.QueryTest
import org.scalatest.BeforeAndAfterAll

import org.apache.carbondata.core.constants.CarbonCommonConstants
import org.apache.carbondata.core.util.CarbonProperties

/**
  * Test cases for testing columns having \N or \null values for non numeric columns
  */
class TestNullValueSerialization extends QueryTest with BeforeAndAfterAll {

  override def beforeAll {
    sql("drop table if exists carbonTable")
    sql("drop table if exists hiveTable")
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT,
        CarbonCommonConstants.CARBON_TIMESTAMP_DEFAULT_FORMAT
      )
    val csvFilePath = s"$resourcesPath/nullvalueserialization.csv"
    sql(
      "CREATE TABLE IF NOT EXISTS carbonTable (ID String, date Timestamp, country String, name " +
        "String, phonetype String, serialname String, salary Decimal(17,2))STORED BY 'org.apache" +
        ".carbondata.format'"
    )
    sql(
      "create table if not exists hiveTable(ID String, date Timestamp, country String, name " +
        "String, " +
        "phonetype String, serialname String, salary Decimal(17,2))row format delimited fields " +
        "terminated by ','"
    )
    sql(
      "LOAD DATA LOCAL INPATH '" + csvFilePath + "' into table carbonTable OPTIONS " +
        "('FILEHEADER'='ID,date," +
        "country,name,phonetype,serialname,salary')"
    )
    sql(
      "LOAD DATA local inpath '" + csvFilePath + "' INTO table hiveTable"
    )
  }


  test("test detail query on column having null values") {
    System.out.println("Carbon Table")
    sql("select * from carbonTable").show()
    System.out.println("Hive Table")
    sql("select * from hiveTable").show()
    checkAnswer(
      sql("select * from carbonTable"),
      sql("select * from hiveTable")
    )
  }

    test("test filter query on column is null") {
      checkAnswer(
        sql("select * from carbonTable where salary is null"),
        sql("select * from hiveTable where salary is null")
      )
    }

    test("test filter query on column is not null") {
      checkAnswer(
        sql("select * from carbonTable where salary is not null"),
        sql("select * from hiveTable where salary is not null")
      )
    }

    test("test filter query on columnValue=null") {
      checkAnswer(
        sql("select * from carbonTable where salary=null"),
        sql("select * from hiveTable where salary=null")
      )
    }

    test("test filter query where date is null") {
      checkAnswer(
        sql("select * from carbonTable where date is null"),
        sql("select * from hiveTable where date is null")
      )
    }

  override def afterAll {
    sql("drop table if exists carbonTable")
    sql("drop table if exists hiveTable")
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "dd-MM-yyyy")
  }
}
