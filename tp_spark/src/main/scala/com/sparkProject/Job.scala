package com.sparkProject

import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType};
import scala.collection.mutable.ListBuffer
import org.apache.spark.sql.functions._

// Jar diretory
// /Users/Bense/Documents/Exercices/Spark/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar
// spark://dhcpwifi-23-246.enst.fr:7077

object Job {

  def main(args: Array[String]): Unit = {

    // SparkSession configuration
    val spark = SparkSession
      .builder
      .appName("spark session TP_parisTech")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._


    /********************************************************************************
      *
      *        TP 1
      *
      *        - Set environment, InteliJ, submit jobs to Spark
      *        - Load local unstructured data
      *        - Word count , Map Reduce
      ********************************************************************************/



    // ----------------- word count ----------------------- //

    // Question 1 : Charger le fichier CSV
    val df = spark.read.option("header","true")
                      .option("delimiter", ",")
                      .option("comment", "#")
                      .option("inferSchema", "true")
                      .csv("/Users/Bense/Documents/Exercices/Spark/TP1_files/cumulative.csv")

    // Question 2 : Nombre de lignes et colonnes
    println("\n\n\n\n ****************** Number of rows : " + df.count)
    println("\n\n\n\n ****************** Number of columns : " + df.columns.length)
    println("\n\n\n\n\n\n")


    // Question 4 : Afficher le DataFrame sous forme de table :
    df.show(5)
    println("\n\n\n\n\n\n")

    // Question 5 : Afficher schema du dataFrame :
    df.printSchema()
    println("\n\n\n\n\n\n")

    // Question 6 : taille des colonnes

    println("\n\n\n\n\n\n")
    println("Equilibrage : ")
    df.groupBy($"koi_disposition").count().show()

    println("\n\n\n\n\n\n")


    // Cleaning !!
    // Question a
    println("\n\n\n\n\n\n")
    println("Question a")
    val df_cleaned =  df.filter($"koi_disposition" === "CONFIRMED" || $"koi_disposition" === "FALSE POSITIVE")
    df_cleaned.show(5)
    println("\n\n\n\n\n\n")

    // Question b
    println("\n\n\n\n\n\n")
    println("Question b")
    df_cleaned.groupBy($"koi_eccen_err1").count().show()
    println("\n\n\n\n\n\n")

    // Question c
    println("\n\n\n\n\n\n")
    println("Question c")
    val df_cleaned2 = df_cleaned.drop($"koi_eccen_err1")
    df_cleaned2.show
    println("\n\n\n\n\n\n")

    // Question d
    val list_columns_drop: List[String] = List("index","kepid","koi_fpflag_nt","koi_fpflag_ss","koi_fpflag_co","koi_fpflag_ec",
      "koi_sparprov","koi_trans_mod","koi_datalink_dvr","koi_datalink_dvs","koi_tce_delivname",
      "koi_parm_prov","koi_limbdark_mod","koi_fittype","koi_disp_prov","koi_comment","kepoi_name","kepler_name",
      "koi_vet_date","koi_pdisposition")
    val df_final = df_cleaned2.drop(list_columns_drop:_*)
    df_final.show(5)

    // Question e
    println("\n\n\n\n\n\n")
    println("Question e")
    var useless_cols = new ListBuffer[String]()
    for(col <- df_final.columns){
      if(df_final.select(col).distinct().count() <= 1){
        useless_cols += col
      }
    }


    // Question f
    println("\n\n\n\n\n\n")
    println("Question f")
    val df_final_2 = df_final.drop(useless_cols:_*)
    println("DF FINAL 2 : " + df_final_2.count())


    // Question g
    println("\n\n\n\n\n\n")
    println("Question g")
    val df_filled = df_final_2.na.fill(0.0)
    println("DF FILED :" + df_filled.count() )



    // Question 6 :
    println("\n\n\n\n\n\n")
    println("Question 6 -------------")
    val df_labels = df_filled.select("rowid", "koi_disposition")
    val df_features = df_filled.drop("koi_disposition")

    println("DF LABELES : " + df_labels.count())
    println("DF FEATURES : " + df_features.count())


    // Jointure
    val df_joined = df_features.join(df_labels, usingColumn = "rowid")
    println("DF JOINED : " + df_joined.count())


    // Question 8 :

    def udf_sum = udf((col1: Double, col2: Double) => col1 + col2)


    val df_newFeatures = df_joined
      .withColumn("koi_ror_min", udf_sum($"koi_ror", $"koi_ror_err2"))
      .withColumn("koi_ror_max", $"koi_ror" + $"koi_ror_err1")


    // Save Question 9
    df_newFeatures
      .coalesce(1) // optional : regroup all data in ONE partition, so that results are printed in ONE file
      // >>>> You should not do that in general, only when the data are small enough to fit in the memory of a single machine.
      .write
      .mode("overwrite")
      .option("header", "true")
      .csv("/Users/Bense/Documents/cleanedDataFrame.csv")

    /********************************************************************************
      *
      *        TP 2 : début du projet
      *
      ********************************************************************************/



  }


}
