

## Installation

1.  Install CPLEX
2.  Install cplex.jar in maven:
    ```sh 
    mvn install:install-file -DgroupId=cplex -DartifactId=cplex -Dversion=12.7.0 -Dpackaging=jar -Dfile=C:\Program Files\IBM\ILOG\CPLEX_Studio129\cplex\lib\cplex.jar
    ```
    
## Compile

```sh
mvn package
```

## Start experiments with CVI-SimPy

1.  Start CVI-Sim simulator:
    ```sh
    java -Djava.library.path=/opt/ibm/ILOG/CPLEX_Studio127/cplex/bin/x86-64_linux/ -jar target/CVI-Sim-0.0.1-SNAPSHOT-jar-with-dependencies.jar [Number of data-centers] [number of requests]
    ```
2. Start CVI-SimPy:
    ```sh
   python3 baseline.py
    ```
   For deep learning implementation run:
    ```sh
   python3 baseline_dqn.py
    ```