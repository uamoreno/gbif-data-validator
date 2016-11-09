package org.gbif.validation.api;

import org.gbif.dwc.terms.Term;
import org.gbif.validation.api.model.FileFormat;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Represents a single source data file and its "metadata".
 */
public class DataFile {

  private Path filePath;
  private FileFormat fileFormat;
  private String sourceFileName;

  private Term[] columns;
  private Term rowType;

  private Character delimiterChar;
  private Integer numOfLines;
  private Integer fileLineOffset;
  private boolean hasHeaders;

  public Character getDelimiterChar() {
    return delimiterChar;
  }

  public void setDelimiterChar(Character delimiterChar) {
    this.delimiterChar = delimiterChar;
  }

  public Term[] getColumns() {
    return columns;
  }

  public void setColumns(Term[] columns) {
    this.columns = columns;
  }

  public Term getRowType() {
    return rowType;
  }

  public void setRowType(Term rowType) {
    this.rowType = rowType;
  }

  public void setFilePath(Path filePath) {
    this.filePath = filePath;
  }

  public void setNumOfLines(Integer numOfLines) {
    this.numOfLines = numOfLines;
  }

  /**
   * Name of the filed as received. For safety reason this name should only be used to display.
   * @param sourceFileName
   */
  public void setSourceFileName(String sourceFileName) {
    this.sourceFileName = sourceFileName;
  }

  public Integer getNumOfLines() {
    return numOfLines;
  }

  /**
   * File name as provided. For safety reason this name should only be used to display.
   *
   */
  @Nullable
  public String getSourceFileName() {
    return sourceFileName;
  }

  /**
   * Path to generated file name.
   *
   * @return safe, path to generated filename
   */
  public Path getFilePath() {
    return filePath;
  }

  /**
   * If this {@link DataFile} represents a part of a bigger file, the offset of lines relative to the
   * source file.
   * @return
   */
  public Integer getFileLineOffset() {
    return fileLineOffset;
  }

  public void setFileLineOffset(Integer fileLineOffset) {
    this.fileLineOffset = fileLineOffset;
  }

  public FileFormat getFileFormat() {
    return fileFormat;
  }

  public void setFileFormat(FileFormat fileFormat) {
    this.fileFormat = fileFormat;
  }

  public boolean isHasHeaders() {
    return hasHeaders;
  }

  public void setHasHeaders(boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  @Override
  public String toString() {
    return "DataFile{" +
            "filePath=" + filePath +
            "fileFormat=" + fileFormat +
            "sourceFileName=" + sourceFileName +
            ", columns=" + Arrays.toString(columns) +
            ", rowType=" + rowType +
            ", delimiterChar='" + delimiterChar + '\'' +
            ", numOfLines=" + numOfLines +
            ", fileLineOffset=" + fileLineOffset +
            ", hasHeaders=" + hasHeaders +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataFile dataFile = (DataFile) o;
    return hasHeaders == dataFile.hasHeaders &&
            Objects.equals(delimiterChar, dataFile.delimiterChar) &&
            Arrays.equals(columns, dataFile.columns) &&
            Objects.equals(rowType, dataFile.rowType) &&
            Objects.equals(filePath, dataFile.filePath) &&
            Objects.equals(fileFormat, dataFile.fileFormat) &&
            Objects.equals(sourceFileName, dataFile.sourceFileName) &&
            Objects.equals(numOfLines, dataFile.numOfLines) &&
            Objects.equals(fileLineOffset, dataFile.fileLineOffset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
            filePath,
            fileFormat,
            sourceFileName,
            columns,
            rowType,
            delimiterChar,
            numOfLines,
            fileLineOffset,
            hasHeaders);
  }

}
