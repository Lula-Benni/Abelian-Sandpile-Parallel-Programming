# Makefile for compiling and running serial program

# Directories
#SRC_DIR = src/serialAbelianSandpile
#BIN_DIR = bin/serialAbelianSandpile
SRC_DIR = src/parallelAbelianSandpile
BIN_DIR = bin/parallelAbelianSandpile

# Source files
JAVA_FILES = $(wildcard $(SRC_DIR)/*.java)

# Compiled class files
CLASS_FILES = $(patsubst $(SRC_DIR)/%.java, $(BIN_DIR)/%.class, $(JAVA_FILES))

# Compilation flags
JAVAC_FLAGS = -d bin -sourcepath src

# Main class
#MAIN_CLASS = serialAbelianSandpile.AutomatonSimulation
MAIN_CLASS = parallelAbelianSandpile.ParallelAutomatonSimulation

# Default arguments (update these if needed)
#ARGS ?= input/1001_by_1001_all_8.csv output/current.png  # Replace 'default_arguments' with your specific default arguments, if any
# Arguments list (update these with your specific arguments)
ARGS_LIST = \
    "input/8_by_8_all_4_copy.csv output/8_by_8_all_4.png" \
    "input/16_by_16_all_4.csv output/16_by_16_all_4.png" \
    "input/16_by_16_one_100.csv output/16_by_16_one_100.png" \
    "input/65_by_65_all_4.csv output/65_by_65_all_4.png" \
    "input/517_by_517_centre_534578.csv output/517_by_517_centre_534578.png" \
    "input/1001_by_1001_all_8.csv output/1001_by_1001_all_8.png"

# Targets
.PHONY: all clean run directories

all: directories $(CLASS_FILES)

directories:
	@mkdir -p $(BIN_DIR)

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	javac $(JAVAC_FLAGS) $<

clean:
	rm -rf bin/*

run: all
	#java -classpath bin $(MAIN_CLASS) $(ARGS)
	for args in $(ARGS_LIST); do \
		echo "Running with arguments: $$args"; \
		java -classpath bin $(MAIN_CLASS) $$args; \
	done
