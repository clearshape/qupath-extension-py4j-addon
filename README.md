# QuPath Py4J extension addon 

Add several methods I found useful to [QuPath Py4J extension](https://github.com/qupath/qupath-extension-py4j)

[QuPath Py4J extension](https://github.com/qupath/qupath-extension-py4j) is bundled for simplicity.
Let me know if that is a problem.

Feel free to use it anyway you want and many thanks for the great works of
[QuPath Py4J extension](https://github.com/qupath/qupath-extension-py4j) and
[Py4J](https://www.py4j.org/)


## Installation

Assuming your QuPath extension folder is at ~/QuPath/v0.5/extensions -
```bash
git clone https://github.com/qupath/qupath-extension-py4j-addon  
cd qupath-extension-py4j-addon
./gradlew clean
./gradlew build
./gradlew copyDependencies
cp ./build/libs/*.jar ~/QuPath/v0.5/extensions
```

## Usage
Python -
```python
from py4j.java_gateway import JavaGateway

gateway = JavaGateway()     # connect to QuPath
QPEx = gateway.entry_point
```

QuPath -
```groovy
import static qupath.ext.py4j.core.QuPathEntryPointAddon.*
```

