package org.gbif.validation.source;

import org.gbif.dwc.terms.Term;
import org.gbif.dwc.terms.TermFactory;
import org.gbif.dwca.io.Archive;
import org.gbif.dwca.io.ArchiveFactory;
import org.gbif.dwca.io.ArchiveField;
import org.gbif.dwca.io.ArchiveFile;
import org.gbif.dwca.io.UnsupportedArchiveException;
import org.gbif.validation.api.TermIndex;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Adapter for extracting information about DarwinCore Archive.
 * The adapter can work on the core file, an extension file or a portion of one of them (after splitting).
 *
 */
public class DwcAdapter {

  public static final Term DEFAULT_ID_TERM = TermFactory.instance().findTerm("ARCHIVE_RECORD_ID");

  private final Archive archive;

  //could be the core or an extension
  private final ArchiveFile darwinCoreComponent;
  private final List<ArchiveField> archiveFields;
  private final Term[] headers;

  private Optional<Map<Term, String>> defaultValues = Optional.empty();

  /**
   * Get a new Reader for the core component of the Dwc-A.
   *
   * @param dwcFolder
   * @throws IOException
   */
  public DwcAdapter(File dwcFolder) throws IOException {
    this(dwcFolder, Optional.empty());
  }

  /**
   * Get a new Reader for an extension of the Dwc-A or the core if rowType is not provided.
   *
   * @param dwcFolder
   * @param rowType can be null to get the core
   * @throws IOException
   */
  public DwcAdapter(File dwcFolder, Optional<Term> rowType) throws IOException {
    Objects.requireNonNull(dwcFolder, "dwcFolder shall be provided");
    archive = ArchiveFactory.openArchive(dwcFolder);

    darwinCoreComponent = (!rowType.isPresent() || archive.getCore().getRowType().equals(rowType.get()))?
                                                  archive.getCore() : archive.getExtension(rowType.get());

    archiveFields = darwinCoreComponent.getFieldsSorted();

    //check if there is default value(s) defined
    archiveFields.stream().filter(af -> af.getIndex() == null)
            .forEach(af -> addDefaultValue(af.getTerm(), af.getDefaultValue()));
    headers = extractHeaders();
  }

  public ArchiveFile getCore() {
    return archive.getCore();
  }

  /**
   * Get a Set of the extensions registered in this archive.
   *
   * @return never null
   */
  public Set<ArchiveFile> getExtensions(){
    return archive.getExtensions();
  }

  @Nullable
  public Term[] getHeaders() {
    return headers;
  }


  /**
   * The purpose is this method is to extract headers from the DarwinCore Archive.
   * The size of the array will be determined by the maximum value of "index" in the definition of the archive.
   * If the "id" is not mapped to any Term, the term DEFAULT_ID_TERM will be assigned to it.
   *
   * @return
   * @throws UnsupportedArchiveException
   */
  private Term[] extractHeaders() throws UnsupportedArchiveException {
    if (archiveFields == null) {
      return null;
    }

    List<ArchiveField> archiveFieldsWithIndex = archiveFields.stream().filter(af -> af.getIndex() != null)
            .collect(Collectors.toList());

    //we assume the id is provided (it is mandatory by the schema)
    Integer idIndex = darwinCoreComponent.getId().getIndex();

    int maxIndex = archiveFieldsWithIndex.stream()
            .mapToInt(ArchiveField::getIndex).max().getAsInt();
    maxIndex = Math.max(maxIndex, darwinCoreComponent.getId().getIndex());

    Term[] terms = new Term[maxIndex + 1];
    // handle id column, assign default Term, it will be rewritten below if assigned to a term
    terms[idIndex] = DEFAULT_ID_TERM;
    archiveFieldsWithIndex.stream().forEach(af -> terms[af.getIndex()] = af.getTerm());
    return terms;
  }

  private void addDefaultValue(Term term, String value){
    if(!defaultValues.isPresent()){
      defaultValues = Optional.of(new HashMap<>());
    }
    defaultValues.get().put(term, value);
  }

  public Optional<Map<Term, String>> getDefaultValues() {
    return defaultValues;
  }

  public Term getRowType() {
    if (darwinCoreComponent == null) {
      return null;
    }
    return darwinCoreComponent.getRowType();
  }

  /**
   * Get the {@link Term} representing the "id" of the current rowType.
   *
   * @return
   */
  public Optional<TermIndex> getRecordIdentifier() {
    if (darwinCoreComponent == null || darwinCoreComponent.getId() == null) {
      return Optional.empty();
    }

    return Optional.of(new TermIndex(darwinCoreComponent.getId().getIndex(),
            headers[darwinCoreComponent.getId().getIndex()]));
  }

}