package com.sparkProject
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{SparkSession}
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.feature.{StringIndexer}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator}

/**
  * Created by Benseddik Mohammed on 27/10/2016.
  */

/**
  * Command spark-submit :
  * ./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp" --driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML /Users/Bense/Documents/Exercices/Spark/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar /Users/Bense/Documents/cleanedDataFrame.csv /Users/Bense/Desktop/stars.model
  */

object JobML {

  def main(args: Array[String]): Unit = {

    // SparkSession configuration
    val spark = SparkSession
      .builder
      .appName("spark session TP_parisTech - ML")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._

    // Example of paths :
    // val cleanedDataFramePath = "/Users/Bense/Documents/cleanedDataFrame.csv"
    // val savedModelPath = "/Users/Bense/Desktop/stars.model"

    // Initializing Paths
    var cleanedDataFramePath = ""
    var savedModelPath = ""

    // Giving Paths
    if(args.length != 2)
      {
        println("\n\n ERROR *************************************************************!!")
        println("Missing Arguments ! Give the path of \" CleanedData \" as First argument.")
        println("and the Directory to save the Model at the end as the Second Argument.")
        sys.exit(0)
      }
    else
      {
        cleanedDataFramePath = args(0)
        savedModelPath = args(1)
      }

    /*********************************************************************************
      *                                                                              *
      *        Exercice 1                                                            *
      *                                                                              *
      ********************************************************************************/

    // Load the CSV File :
    val df = spark.read.option("header","true")
      .option("delimiter", ",")
      .option("comment", "#")
      .option("inferSchema", "true")
      .csv(cleanedDataFramePath)


    // Number of Lines and columns
    println("\n\n\n\n Number of rows : " + df.count + "*************************************************************")
    println("\n\n\n\n Number of columns : " + df.columns.length + "*************************************************")
    println("\n\n")

    // Drop the rowid column
    df.drop("rowid")
    val cols = df.columns.filter(_ != "koi_disposition")

    // Vector Assembler
    println("Vector Assembler *************************************************************")
    val assembler = new VectorAssembler()
      .setInputCols(cols)
      .setOutputCol("features")

    // Transform the Dataframe to the VectorAssembler Form
    val df_output = assembler.transform(df)
    println(df_output.select("features", "koi_disposition").first())
    println("\n\n")

    // Centrer & Scale the Data
    println("Scaled Data *************************************************************")
    val scaler = new StandardScaler().setInputCol("features")
              .setOutputCol("scaledFeatures").setWithStd(true).setWithMean(false)
    val scalerModel = scaler.fit(df_output)
    val scaledData = scalerModel.transform(df_output)
    scaledData.drop("features")
    println(scaledData.select("scaledFeatures", "koi_disposition").first())
    println("\n\n")

    // One hot encoding - Indexed Data
    println("Indexed Data *************************************************************")
    val indexer = new StringIndexer()
      .setInputCol("koi_disposition")
      .setOutputCol("labels")
      .fit(scaledData)
    val indexedScaledData = indexer.transform(scaledData)
    println(indexedScaledData.select("scaledFeatures", "labels").first())
    println("\n\n")


    /*********************************************************************************
      *                                                                              *
      *        Exercice 2                                                            *
      *                                                                              *
      ********************************************************************************/
    // Split training & test for the First time (90 - 10)
    println("Split Data into training and test Parts ****************************************************")
    val Array(training, test) = indexedScaledData.randomSplit(Array(0.9, 0.1))
    println("\n\n")

    // Creating the Logistic Regression Model and initializing the parameters
    val modelLR = new LogisticRegression()

    modelLR.setElasticNetParam(1.0)  // L1-norm regularization : LASSO
      .setLabelCol("labels")
      .setFeaturesCol("scaledFeatures")
      .setStandardization(true)  // to scale each feature of the model
      .setFitIntercept(true)  // we want an affine regression (with false, it is a linear regression)
      .setTol(1.0e-5)  // stop criterion of the algorithm based on its convergence
      .setMaxIter(300)  // a security stop criterion to avoid infinite loops

    // Creating the array of Search Grid with Logarithmic Scale
    println("Initializing the Grid array *************************************************************")
    val array = -6.0 to (0.0, 0.5) toArray
    val arrayLog = array.map(x => math.pow(10,x))

    val paramGrid = new ParamGridBuilder()
      .addGrid(modelLR.regParam, arrayLog)
      .build()

    // Creating the BinaryClassificationEvaluator
    val evaluator = new BinaryClassificationEvaluator().setLabelCol("labels")

    // Spliting the data for the second time (70 30)
    val trainValidationSplit = new TrainValidationSplit()
      .setEstimator(modelLR)
      .setEvaluator(evaluator)
      .setEstimatorParamMaps(paramGrid)
      // 70% of the data will be used for training and the remaining 30% for validation.
      .setTrainRatio(0.7)

    // Run train validation split, and choose the best set of parameters.
    println("Runining the model *************************************************************")
    val model = trainValidationSplit.fit(training)

    // Make predictions on test data. model is the model with combination of parameters
    // that performed best.
    val df_WithPredictions = model.transform(test).select("scaledFeatures", "labels", "prediction")
    df_WithPredictions.show()
    df_WithPredictions.groupBy("labels", "prediction").count.show()

    // Printing the score :
    println("The score : ****************************************************************")
    evaluator.setRawPredictionCol("prediction")
    println(evaluator.evaluate(df_WithPredictions) + "\n\n\n")

    // Saving the file
    model.write.overwrite().save(savedModelPath)
  }
}
