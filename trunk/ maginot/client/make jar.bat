path="C:\Program Files (x86)\Java\jdk1.6.0_11\bin"
del *.class
javac *.java
jar cmf mainClass.txt "Maginot Client.jar" *.class
pause