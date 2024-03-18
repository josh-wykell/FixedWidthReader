# FixedWidthReader
## A tool to read fixed width files without changing them
Fixed width files are files that use column indexes instead of delimiters to store data. 
They are difficult to view to debug when they contain bad data. 
This is a tool to provide a configuration of column names to column widths and convert a fixed width file into a standard table view.
### Currently basic proof of concept is complete
* Takes in a fixed width file and a yaml file as arguments
* Yaml file arg is a simple config with the column name as the key and the size of the field in the fixed width row as the value
### ToDo's
* Add validations 
* Add form to input arguments instead of command line only
* Add ability to save and select yaml config
