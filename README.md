# ExoPlanetsClassification
 Practical work for Telecom ParisTech's Big Data program. Classification of Exo Planets using Spark/Logistic Regression and Lasso Penalisation.


 # How to run :
 Build project : build assembly
 Run the project : "./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp" --driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML /Users/Bense/Documents/Exercices/Spark/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar /Users/Bense/Documents/cleanedDataFrame.csv /Users/Bense/Desktop/stars.model"
