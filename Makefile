JAVAC = javac
JAVA = java
BIN_DIR = bin
CLASSPATH = $(BIN_DIR);lib/sqlite-jdbc.jar
SRC_DIR = src
UI_DIR = $(SRC_DIR)/ui
LOGIC_DIR = $(SRC_DIR)/logic
DATABASE_DIR = $(SRC_DIR)/database
UTILS_DIR = $(SRC_DIR)/utils
MAIN_CLASS = Main

SRC_FILES = $(wildcard $(SRC_DIR)/*.java) \
            $(wildcard $(UI_DIR)/*.java) \
            $(wildcard $(LOGIC_DIR)/*.java) \
            $(wildcard $(DATABASE_DIR)/*.java) \
            $(wildcard $(UTILS_DIR)/*.java)

# Default target
all: compile

# Compile all Java files
compile:
	@echo "Compiling Java source files..."
	@if not exist "$(BIN_DIR)" mkdir "$(BIN_DIR)"
	$(JAVAC) -cp "lib/sqlite-jdbc.jar" -sourcepath $(SRC_DIR) -d $(BIN_DIR) $(SRC_FILES)
	@echo "Compilation complete!"

# Run the application
run:
	@echo "Starting IMDb Database System..."
	$(JAVA) -cp $(CLASSPATH) $(MAIN_CLASS)

# Clean compiled files
clean:
	@echo "Cleaning compiled files..."
	@if exist "$(BIN_DIR)" rmdir /S /Q "$(BIN_DIR)"
	@echo "Clean complete!"

.PHONY: all compile run clean 
