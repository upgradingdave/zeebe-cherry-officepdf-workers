/* ******************************************************************** */
/*                                                                      */
/*  OfficeToPdfWorker                                                   */
/*                                                                      */
/*  Get an office (Microsoft, Open Office) and transform it to PDF      */
/* ******************************************************************** */
package org.camunda.cherry.pdf;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.camunda.cherry.definition.AbstractWorker;
import org.camunda.cherry.definition.filevariable.FileVariable;
import org.camunda.cherry.definition.filevariable.FileVariableFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


@Component
public class OfficeToPdfWorker extends AbstractWorker {

    public static final String BPMERROR_CONVERSION_ERROR = "CONVERSION_ERROR";
    public static final String BPMERROR_LOAD_FILE_ERROR = "LOAD_FILE_ERROR";
    private static final String INPUT_SOURCE_FILE = "sourceFile";
    private static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
    private static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
    private static final String OUTPUT_DESTINATION_FILE = "destinationFile";
    public static final String WORKERTYPE_PDF_CONVERT_TO = "c-pdf-convert-to";

    public OfficeToPdfWorker() {
        super(WORKERTYPE_PDF_CONVERT_TO,
                Arrays.asList(
                        AbstractWorker.WorkerParameter.getInstance(INPUT_SOURCE_FILE, Object.class, Level.REQUIRED, "FileVariable for the file to convert"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_FILE_NAME, String.class, Level.REQUIRED, "Destination file name"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_STORAGEDEFINITION, String.class, FileVariableFactory.FileVariableStorage.JSON.toString(), Level.OPTIONAL, "Storage Definition use to describe how to save the file")

                ),
                Arrays.asList(
                        AbstractWorker.WorkerParameter.getInstance(OUTPUT_DESTINATION_FILE, Object.class, Level.REQUIRED, "FileVariable converted")
                ),
                Arrays.asList(BPMERROR_CONVERSION_ERROR, BPMERROR_LOAD_FILE_ERROR));
    }

    @Override
    @ZeebeWorker(type = WORKERTYPE_PDF_CONVERT_TO, autoComplete = true)
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

        FileVariable sourceFileVariable = getFileVariableValue(INPUT_SOURCE_FILE,  activatedJob);

        String destinationFileName = getInputStringValue(INPUT_DESTINATION_FILE_NAME, null, activatedJob);
        String destinationStorageDefinition = getInputStringValue(INPUT_DESTINATION_STORAGEDEFINITION, null, activatedJob);

        if (sourceFileVariable == null || sourceFileVariable.value == null) {
            throw new ZeebeBpmnError(BPMERROR_LOAD_FILE_ERROR, "Worker [" + getName() + "] cannot read file[" + INPUT_SOURCE_FILE + "]");
        }

        // get the file
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourceFileVariable.value);

        final IXDocReport report;
        try {
            report = XDocReportRegistry.getRegistry()
                    .loadReport(byteArrayInputStream, TemplateEngineKind.Velocity);

            final IContext context = report.createContext();

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
           
            report.convert(context,
                    Options.getTo(ConverterTypeTo.PDF),
                    out);
            FileVariable fileVariableOut = new FileVariable();
            fileVariableOut.value = out.toByteArray();
            fileVariableOut.name = destinationFileName;
            setFileVariableValue(OUTPUT_DESTINATION_FILE, destinationStorageDefinition, fileVariableOut, contextExecution);

        } catch (Exception e) {
            throw new ZeebeBpmnError(BPMERROR_CONVERSION_ERROR, "Worker [" + getName() + "] cannot convert file[" + sourceFileVariable.name + "] : " + e);
        }

    }
}