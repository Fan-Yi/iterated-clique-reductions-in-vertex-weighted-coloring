graph_dir="benchmark_graphs/"
# input_dir="bio-yeast/bio-yeast"
input_dir="C/C2000.5.wcol"
output_dir="output_graphs"

java="openjdk.java"

# program instance seed output_folder cut_off
# $java -Xmx16384m MinWGCPreprocessing $graph_dir$input_dir 7 $output_dir 300
$java -Xmx24576m MinWGCPreprocessing $graph_dir$input_dir 7 $output_dir 1200

