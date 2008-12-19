path="C:\Program Files\Java\jdk1.6.0_06\bin"
del *.class
javac *.java
jar cmf mainClass.txt "Maginot Client.jar" *.class
pause