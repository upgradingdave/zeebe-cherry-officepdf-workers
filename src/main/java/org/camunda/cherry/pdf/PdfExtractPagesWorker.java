/* ******************************************************************** */
/*                                                                      */
/*  PdfExtractPagesWorker                                                   */
/*                                                                      */
/*  Extract pages from a PDF, and give back a PDF                       */
/* ******************************************************************** */
package org.camunda.cherry.pdf;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.camunda.cherry.definition.AbstractWorker;
import org.camunda.cherry.definition.filevariable.FileVariable;
import org.camunda.cherry.definition.filevariable.FileVariableFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;


@Component
public class PdfExtractPagesWorker extends PdfWorker {

    public static final String BPMERROR_ENCRYPTED_NOT_SUPPORTED = "ENCRYPTED_NOT_SUPPORTED";
    public static final String BPMERROR_LOAD_FILE_ERROR = "LOAD_FILE_ERROR";
    public static final String BPMERROR_INVALID_EXPRESSION = "INVALID_EXPRESSION";
    public static final String BPMERROR_SAVE_ERROR = "DESTINATION_SAVE_ERROR";

    private static final String INPUT_SOURCE_FILE = "sourceFile";
    private static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
    private static final String INPUT_EXTRACT_EXPRESSION = "extractExpression";
    private static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
    private static final String OUTPUT_DESTINATION_FILE = "destinationFile";
    public static final String WORKERTYPE_PDF_EXTRACTPAGES = "c-pdf-extractpages";

    // merge https://pdfbox.apache.org/docs/2.0.0/javadocs/org/apache/pdfbox/multipdf/PDFMergerUtility.html

    public PdfExtractPagesWorker() {
        super(WORKERTYPE_PDF_EXTRACTPAGES,
                Arrays.asList(
                        AbstractWorker.WorkerParameter.getInstance(INPUT_SOURCE_FILE, "Source file", Object.class, Level.REQUIRED, "FileVariable for the file to convert"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_EXTRACT_EXPRESSION, "Extract expression", String.class, Level.REQUIRED, "Extract pilot. Example, 2-4 mean extract pages 2 to 4 (document page start at 1). Use 'n' to specify the end of the document (2-n) extract from page 2 to the end. Simple number is accepted to extract a page. Example: 4-5, 10, 15-n or 2-n, 1 (first page to the end)"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_FILE_NAME, "File name,", String.class, Level.REQUIRED, "Destination file name"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_STORAGEDEFINITION, "Destination Storage definition", String.class, FileVariableFactory.FileVariableStorage.JSON.toString(), Level.OPTIONAL, "Storage Definition use to describe how to save the file")

                ),
                Collections.singletonList(
                        AbstractWorker.WorkerParameter.getInstance(OUTPUT_DESTINATION_FILE, "Destination variable name", Object.class, Level.REQUIRED, "FileVariable converted")
                ),
                Arrays.asList(AbstractWorker.BpmnError.getInstance(BPMERROR_ENCRYPTED_NOT_SUPPORTED, "PDF Encrypted not supported"),
                        AbstractWorker.BpmnError.getInstance(BPMERROR_LOAD_FILE_ERROR, "Load file error"),
                        AbstractWorker.BpmnError.getInstance(BPMERROR_INVALID_EXPRESSION, "Invalid expression"),
                        AbstractWorker.BpmnError.getInstance(BPMERROR_SAVE_ERROR, "Save error")
                ));
    }

    @Override
    @ZeebeWorker(type = WORKERTYPE_PDF_EXTRACTPAGES, autoComplete = true)
    public void handleWorkerExecution(final JobClient jobClient, final ActivatedJob activatedJob) {
        super.handleWorkerExecution(jobClient, activatedJob);
    }


    /**
     * @param jobClient        client
     * @param activatedJob     job activated
     * @param contextExecution context of this execution
     */
    @Override
    public void execute(final JobClient jobClient, final ActivatedJob activatedJob, ContextExecution contextExecution) {

        FileVariable sourceFileVariable = getFileVariableValue(INPUT_SOURCE_FILE, activatedJob);

        String destinationFileName = getInputStringValue(INPUT_DESTINATION_FILE_NAME, null, activatedJob);
        String destinationStorageDefinition = getInputStringValue(INPUT_DESTINATION_STORAGEDEFINITION, null, activatedJob);

        if (sourceFileVariable == null || sourceFileVariable.value == null) {
            throw new ZeebeBpmnError(BPMERROR_LOAD_FILE_ERROR, "Worker [" + getName() + "] cannot read file[" + INPUT_SOURCE_FILE + "]");
        }

        String extractExpression = getInputStringValue(INPUT_EXTRACT_EXPRESSION, "1-n", activatedJob);
        // get the file
        PDDocument sourceDocument = null;
        PDDocument destinationDocument = null;
        int nbPagesExtracted = 0;
        try {
            try {
                sourceDocument = PDDocument.load(sourceFileVariable.value);
            } catch (Exception e) {
                throw new ZeebeBpmnError(BPMERROR_LOAD_FILE_ERROR, "Worker [" + getName() + "] Can't load document [" + sourceFileVariable.name + "]");
            }
            if (sourceDocument.isEncrypted()) {
                throw new ZeebeBpmnError(BPMERROR_ENCRYPTED_NOT_SUPPORTED, "Worker [" + getName() + "] Document is encrypted");
            }
            destinationDocument = new PDDocument();
            // replace any "n" information in the expression by the number of page
            String extractExpressionResolved = extractExpression.replaceAll("n", String.valueOf(sourceDocument.getNumberOfPages()));
            String[] expressionsList = extractExpressionResolved.split(",", 0);
            for (String oneExpression : expressionsList) {
                int firstPage;
                int lastPage;
                // format must be <number1>-<number2> where number1<=number2
                try {
                    String[] expressionDetail = oneExpression.split("-", 2);
                    firstPage = expressionDetail.length >= 1 ? Integer.parseInt(expressionDetail[0]) : -1;
                    lastPage = expressionDetail.length >= 2 ? Integer.parseInt(expressionDetail[1]) : firstPage;

                    if (firstPage == -1 || lastPage == -1 || firstPage > lastPage)
                        throw new ZeebeBpmnError(BPMERROR_INVALID_EXPRESSION, "Worker [" + getName() + "] Expression is <firstPage>-<lastPage> where firstPage<=lastPage :" + firstPage + "," + lastPage);
                } catch (Exception e) {
                    throw new ZeebeBpmnError(BPMERROR_INVALID_EXPRESSION, "Worker [" + getName() + "] Expression must be <firstPage>-<lastPage> : received[" + oneExpression + "] " + e);
                }
                for (int pageIndex = firstPage; pageIndex <= lastPage; pageIndex++) {
                    if (pageIndex <= sourceDocument.getNumberOfPages()) {
                        // getPage starts at 0, pageIndex start at 1
                        destinationDocument.addPage(sourceDocument.getPage(pageIndex - 1));
                        nbPagesExtracted++;
                    }
                }
            }

            saveOutputPdfDocument(destinationDocument, destinationFileName,
                    OUTPUT_DESTINATION_FILE,
                    destinationStorageDefinition,
                    contextExecution);

        } catch (Exception e) {
            logError("During extraction " + e.toString());
        } finally {
            if (sourceDocument != null)
                try {
                    sourceDocument.close();
                } catch (Exception e) {
                    // don't care
                }
            if (destinationDocument != null) {
                try {
                    destinationDocument.close();
                } catch (Exception e) {
                    // don't care
                }

            }
        }
        logInfo("Cherry.PdfExtractPages: extract " + nbPagesExtracted + " pages from document[" + sourceFileVariable.name + "] to [" + destinationFileName + "]");
    }
}
