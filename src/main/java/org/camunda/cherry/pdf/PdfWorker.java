/* ******************************************************************** */
/*                                                                      */
/*  PdfWorker                                                   */
/*                                                                      */
/*  Super class for all PDF Worker, to offer service                    */
/* ******************************************************************** */
package org.camunda.cherry.pdf;

import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.camunda.cherry.definition.AbstractWorker;
import org.camunda.cherry.definition.BpmnError;
import org.camunda.cherry.definition.RunnerParameter;
import org.camunda.cherry.definition.filevariable.FileVariable;

import java.io.ByteArrayOutputStream;
import java.util.List;

public abstract class PdfWorker extends AbstractWorker {
    protected static final String BPMERROR_SAVE_ERROR = "DESTINATION_SAVE_ERROR";

    protected PdfWorker(String name, List<RunnerParameter> listInput,
                        List<RunnerParameter> listOutput,
                        List<BpmnError> listBpmnErrors) {
        super(name, listInput, listOutput, listBpmnErrors);
    }


    /**
     * Save a ¨pdfDocument in an Output Parameter of the worker
     *
     * @param pdDocument                   Pdf document to save
     * @param fileName                     name of the Output document
     * @param outputParameterName          name of the worker parameter (must be defined in the Output Parameters)
     * @param destinationStorageDefinition how to save the document
     */
    protected void saveOutputPdfDocument(PDDocument pdDocument,
                                         String fileName,
                                         String outputParameterName,
                                         String destinationStorageDefinition,
                                         AbstractWorker.ContextExecution contextExecution) throws ZeebeBpmnError {
        FileVariable fileVariableOut = new FileVariable();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            pdDocument.save(byteArrayOutputStream);

            fileVariableOut.value = byteArrayOutputStream.toByteArray();
            fileVariableOut.name = fileName;

            setFileVariableValue(outputParameterName, destinationStorageDefinition, fileVariableOut, contextExecution);
        } catch (Exception e) {
            throw new ZeebeBpmnError(BPMERROR_SAVE_ERROR, "Worker [" + getName() + "] cannot save destination[" + fileName + "] : " + e);
        }
    }
}
