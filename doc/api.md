# API Response
Specifications of the GBIF Data Validator API response.

## Response Example
```json
{
  "indexeable": "[true|false]",
  "fileName": "myoccurrencefile.csv",
  "fileFormat": "[delimited | dwca | excel]",
  "receivedMediaType": "text/csv",
  "validationProfile": "GBIF_INDEXING_PROFILE",
  "errorCode": "INVALID_FILE_FORMAT",
  "errorMessage": "Invalid file format",
  "results": [
    {
      "fileName": "myoccurrencefile.csv",
      "numberOfLines": 18,
      "fileType": "CORE",
      "rowType": "http://rs.tdwg.org/dwc/terms/Occurrence",
      "idTerm": "http://unknown.org/ARCHIVE_RECORD_ID",
      "termsFrequency": [
        {"dwc:occurrenceID": 11}, {"dwc:country": 9}
      ],
      "interpretedValueCounts": {
        "gbif:taxonKey": 0
      },
      "issues": [{}]
    }
  ]
}
```

## Main structure
- `"indexeable"` : Is the provided resource indexeable by GBIF?
- `"fileName"` : File name of the submitted file
- `"fileFormat"` : File format used be the server handle the submitted file
- `"validationProfile"` : Validation profile used to validate the provided resource
- `"errorCode"` : Contains the error code in case the provided resource can not be validated
- `"errorMessage"` : Contains human readable message in case the provided resource can not be validated

### Results structure
- `"fileName"` : File name of the submitted file
- `"numberOfLines"` : Number of lines in the file
- `"fileType"` : Type of file : CORE, EXTENSION or METADATA
- `"rowType"` : rowType based on DarwinCore term
- `"idTerm"` : Term identified as the "id" within the resource.
- `"termsFrequency"` : Contains frequency of all terms in the provided resource as ordered list of key/value.
- `"interpretedValueCounts"` : Contains counts of a preselected interpreted values
- `"issues"` : List of all issues found in the provided resource

## Issues structure

List of possible "issues" can be found [here](https://github.com/gbif/gbif-data-validator/blob/master/doc/evaluation_types.md).

### Resource structure
Represents the result of an evaluation of the structure of the entire resource.

```json
{
  "issue": "RECORD_NOT_UNIQUELY_IDENTIFIED",
  "issueCategory": "RESOURCE_STRUCTURE",
  "count": 2,
  "sample": [
    {
      "recordId": "1"
    },
    {
       "recordId": "5"
    }
  ]
}
```

### Record structure
Represents the result of an evaluation of the structure of a single record
```json
{
  "issue": "COLUMN_COUNT_MISMATCH",
  "count": 1,
  "identifierTerm": "dwc:occurrenceId",
  "sample": [
    {
      "relatedData": {
        "line:": "1",
        "identifier": "occ-1",
        "expected": "90",
        "found": "89"
      }
    }
  ]
}
```

### Record interpretation
Represents the result of an evaluation evaluation of an interpretation remark of a record.
```json
{
  "issue": "RECORDED_DATE_MISMATCH",
  "issueCategory": "OCC_INTERPRETATION_BASED",
  "count": 1,
  "identifierTerm": "dwc:occurrenceId",
  "sample": [
    {
      "lineNumber": 1,
      "relatedData": {
        "identifier": "occ-1",
        "dwc:month": "2",
        "dwc:day": "26",
        "dwc:year": "1996",
        "dwc:eventDate": "1996-01-26T01:00Z"
      }
    }
  ]
}
```

