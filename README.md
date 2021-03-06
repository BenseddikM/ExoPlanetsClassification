# ExoPlanetsClassification
---
A practical work project for Telecom ParisTech's Big Data program. Classification of Exo Planets using SparkML's/Logistic Regression and Lasso Penalization.

## Synopsis
**Goal**  : Create a Binary classifier for exoplanets labeled "confirmed" or "false-positive".

**Context** : Exoplanets are planets rotating around other stars than the sun. This study allows us to better understand how the solar system was formed, and a fraction of them could be conductive to the development of extraterrestrial life.

<p align="center">
  <img src="https://raw.githubusercontent.com/BenseddikM/ExoPlanetsClassification/master/luminosity_curve.png" alt="Luminosity curve" height="300" width="450""/>
</p>

They are detected in two steps:
* A *Satellite* (Kepler) observes the stars and marks those whose luminosity curve shows a "hollow", which could indicate that a planet has passed (part of the light emitted by the star being obscured by the passage of the planet). This method of "transit" allows us to define candidate exoplanets, and to deduce the characteristics that the planet would have if it really existed (distance to its star, diameter, shape of its orbit, etc.).
* It is then necessary to validate or invalidate the candidates using another more expensive method, based on measurements of radial velocities of the star. Candidates are then classified as "confirmed" or "false-positive".

As there are about 200 billion stars in our galaxy, and therefore potentially as much (or even more) exoplanets, their detection must be automated to "scale up". The method of transits is already automatic (more than 22 million curves of luminosity recorded by Kepler), but not the confirmation of the candidate planets, hence the automatic classifier that we will build.

## Data
Data on exoplanets is public and available online (check the [link](http://exoplanetarchive.ipac.caltech.edu/index.html)). There are already 3388 exoplanets confirmed and about as many false positives, our classifier will be trained on these data. There is one exoplanet per line. The column of labels (what we are going to try to predict) is called "koi_disposition". You can retrieve the data in csv format here. The contents of the columns of the dataset are explained here. The classifier will only use information from the brightness curves.

## About the Logistic Regression with Lasso
The classifier we use is based on a logistic regression
([Link for the implementation with Spark Ml](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.ml.classification.LogisticRegression)) with regularization in function of cost which allows us to penalize features having little impact on classification: this is the LASSO method.

The importance of regularization is controlled by a hyper-parameter of the model which must be adjusted by hand. Most machine learning algorithms possess hyper-parameters, such as the number of neurons in a neural network, the number of trees and their depth in random forests, etc. One of the techniques to automatically adjust the hyper-parameters is the grid search:
* Create a grid of values to test for hyper-parameters.
* At each point of the grid, separate the training set into a training set (70%) and a validation set (30%). Train a model on the training set and calculate the model error on the validation set.
* Select the grid point where the validation error is the lowest i.e. where the model best learned. The values of hyper-parameters of this point are then kept.

<p align="center">
  <img src="https://raw.githubusercontent.com/BenseddikM/ExoPlanetsClassification/master/imageSplit.png" alt="Luminosity curve" height="250" width="540""/>
</p>


We have the confusion Matrix and the score :

<center>

| Labels 	| Predictions 	| Count 	|
|--------	|-------------	|-------	|
| 1.0    	| 1.0         	| 238   	|
| 0.0    	| 1.0         	| 10    	|
| 1.0    	| 0.0         	| 10    	|
| 0.0    	| 0.0         	| 360   	|

</center>

```
The obtained score is : 0.966325196163906
```


## Built With
* **IntelliJ IDE** : IDE which allows us to develop data science projects.
* **Spark 2.0.0**  : Maching Learning Framework we used. (most from ML library).
* **Scala** : Programmation Language we used with Spark.

## Contributors :
* [Mohammed BENSEDDIK](https://github.com/BenseddikM)
* [Sami BARGAOUI](https://github.com/Sbargaoui)

## How to run

**Build project** :
```
sbt assembly
```

**Running the project**
* Initial spark-submit command :
```
./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp"  
--driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML  
/Users/Bense/Documents/Exercices/Spark/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar
```
*- Note that we run the job "JobMl" here, there is another job in the project repository named "Job", that cleans the initial data.*

Add two parameters :
* First one would be the path of the cleanedData (also present in the repository), for our case :

```
Users/Bense/Documents/cleanedDataFrame.csv
```

* Second argument is the path & the name for the saved model as an output :
```
/Users/Bense/Desktop/stars.model
```

**Final command** :

```
./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp"  
--driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML  
/Users/Bense/Documents/Exercices/Spark/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar  
Users/Bense/Documents/cleanedDataFrame.csv /Users/Bense/Desktop/stars.model
```
Thus we obtain the computed score and the confusion matrix.
