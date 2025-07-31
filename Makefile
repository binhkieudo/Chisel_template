# Makefile for SmuDma project

# Variables
SBT := sbt
ASCIIDOCTOR := asciidoctor # Tool for HTML generation
ASCIIDOCTOR_PDF := asciidoctor-pdf # Tool for PDF generation
ADOC_SRC := doc/*.adoc
ADOC_HTML := $(ADOC_SRC:.adoc=.html)
ADOC_PDF := $(ADOC_SRC:.adoc=.pdf)
VERILOG_DIR := ./generated
TEST_DIR := ./test_run_dir
PROJECT_DIR := ./project
TARGET_DIR := ./target
BUILD_DIR := ./build

.PHONY: all bore probe rwprobe test doc html pdf clean clean_all

# Default target
all: verilog test doc

# Generate Verilog for SmuDma
bore:
	@$(SBT) generateSimple

probe:
	@$(SBT) generateProbe
	
rwprobe:
	@$(SBT) generateRWProbe
	
# Run SmuDma tests
test:
	@echo "Running tests..."
	@$(SBT) "testOnly smudma.dma.simple.MySmuDmaTester -- -DemitVcd=1"

# Build documentation (HTML and PDF)
# Assumes SmuDma.adoc is in the same directory as the Makefile
doc: html pdf

# Build SmuDma.adoc to HTML
html: $(ADOC_HTML)

$(ADOC_HTML): $(ADOC_SRC)
	@echo "Building $(ADOC_SRC) to $(ADOC_HTML)..."
	@$(ASCIIDOCTOR) $(ADOC_SRC)

# Build SmuDma.adoc to PDF
pdf: $(ADOC_PDF)

$(ADOC_PDF): $(ADOC_SRC)
	@echo "Building $(ADOC_SRC) to $(ADOC_PDF)..."
	@$(ASCIIDOCTOR_PDF) $(ADOC_SRC)

# Clean generated files
clean:
	@echo "Cleaning generated files..."
	@$(SBT) clean
	@rm -rf $(TEST_DIR)
	@rm -rf $(VERILOG_DIR)
	@rm -rf $(BUILD_DIR)
	@rm -f $(ADOC_HTML) $(ADOC_PDF)
	
clean_all:
	@echo "Cleaning generated files..."
	@$(SBT) clean
	@rm -rf $(TEST_DIR)
	@rm -rf $(VERILOG_DIR)
	@rm -rf $(TARGET_DIR)
	@rm -rf $(PROJECT_DIR)
	@rm -rf $(BUILD_DIR)
	@rm -f $(ADOC_HTML) $(ADOC_PDF)