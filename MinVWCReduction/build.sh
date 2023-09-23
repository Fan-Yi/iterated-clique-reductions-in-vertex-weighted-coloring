flags="-Xlint:deprecation -Xlint:unchecked -Xdiags:verbose"

javac="openjdk.javac"

$javac $flags  GraphPack/WeightedVertexPack/VertexPack/Vertex.java \
	GraphPack/WeightedVertexPack/WeightedVertex.java \
	GraphPack/EdgePack/Edge.java \
	GraphPack/GraphFormatParserPack/SimpleUndirectedVertexWeightedGraphDIMACSParser.java \
	GraphPack/SimpleUndirectedVertexWeightedGraphPack/SimpleUndirectedVertexWeightedGraph.java \
	GraphPack/DynamicSimpleUndirectedVertexWeightedGraphPack/DynamicSimpleUndirectedVertexWeightedGraph.java \
	DegreeBucketPack/DegreeBasedPartition.java \
	WeightBucketPack/WeightBasedPartition.java \
	GraphPack/WGCReducedGraph.java \
	DelFolderPack/DeleteFolder.java \
	InterfacePack/IntRelate.java \
	TwoParametersInterfacePack/TwoParametersBehavior.java \
	ZeroParameterInterfacePack/ZeroParameterBehavior.java \
	ConfigPack/Config.java \
	DebugConfigPack/DebugConfig.java \
	MinWGCPreprocessing.java 

# javac $flags MinWGCPreprocessing.java 

# echo "javac $flags"
# echo "	MinWGCPreprocessing.java"





