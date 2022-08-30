/* ******************************************************************** */
/*                                                                      */
/*  GenerateOfficeWorker                                                */
/*                                                                      */
/*  Generate a office document (Microsoft, OpenOffice) from a template  */
/*  and a dictionary ti replace the place holder in the document        */
/* ******************************************************************** */
package org.camunda.cherry.office;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;

@Component
public class GenerateOfficeWorker extends AbstractWorker {

    public static final String BPMERROR_CONVERSION_ERROR = "CONVERSION_ERROR";
    public static final String BPMERROR_LOAD_FILE_ERROR = "LOAD_FILE_ERROR";
    private static final String INPUT_SOURCE_FILE = "sourceFile";
    private static final String INPUT_SOURCE_STORAGEDEFINITION = "sourceStorageDefinition";
    private static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
    private static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
    private static final String INPUT_VARIABLES = "variables";
    private static final String INPUT_VARIABLES_NAMES = "variablesName";


    private static final String OUTPUT_DESTINATION_FILE = "destinationFile";
    public static final String WORKERTYPE_OFFICE_GENERATION = "c-office-generation";

    Logger logger = LoggerFactory.getLogger(GenerateOfficeWorker.class.getName());

    public GenerateOfficeWorker() {
        super(WORKERTYPE_OFFICE_GENERATION,
                Arrays.asList(
                        AbstractWorker.WorkerParameter.getInstance(INPUT_SOURCE_FILE, "Source file", Object.class, Level.REQUIRED, "FileVariable for the file to convert"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_SOURCE_STORAGEDEFINITION, "Source Storage definition", String.class, FileVariableFactory.FileVariableStorage.JSON.toString(), Level.OPTIONAL, "Storage Definition use to access the file"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_FILE_NAME, "Destination file name", String.class, Level.REQUIRED, "Destination file name"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_DESTINATION_STORAGEDEFINITION, "Destination storage defintion", String.class, FileVariableFactory.FileVariableStorage.JSON.toString(), Level.OPTIONAL, "Storage Definition use to describe how to save the file"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_VARIABLES, "Dictionary variables for place holder", Map.class, Level.OPTIONAL, "Template document contains place holders. This is the dictionary which contains values for theses place holder"),
                        AbstractWorker.WorkerParameter.getInstance(INPUT_VARIABLES_NAMES, "Names of variables in the dictionary", String.class, Level.OPTIONAL, "Template document contains place holders. Here the list of variable to add in the dictionary for place holder")

                ),
                Collections.singletonList(
                        AbstractWorker.WorkerParameter.getInstance(OUTPUT_DESTINATION_FILE, "Destination file", Object.class, Level.REQUIRED, "FileVariable converted")
                ),
                Arrays.asList(AbstractWorker.BpmnError.getInstance( BPMERROR_CONVERSION_ERROR, "Conversion error"),
                        AbstractWorker.BpmnError.getInstance(BPMERROR_LOAD_FILE_ERROR, "Load file error")));
    }

    @Override
    @ZeebeWorker(type = WORKERTYPE_OFFICE_GENERATION, autoComplete = true)
    public void handleWorkerExecution(final JobClient jobClient, final ActivatedJob activatedJob) {
        super.handleWorkerExecution(jobClient, activatedJob);
    }


    @Override
    public void execute(final JobClient jobClient, final ActivatedJob activatedJob, ContextExecution contextExecution) {
        String sourceStorageDefinition = getInputStringValue(INPUT_SOURCE_STORAGEDEFINITION, null, activatedJob);
        FileVariable sourceFileVariable = getFileVariableValue(INPUT_SOURCE_FILE, activatedJob);

        String destinationFileName = getInputStringValue(INPUT_DESTINATION_FILE_NAME, null, activatedJob);
        String destinationStorageDefinition = getInputStringValue(INPUT_DESTINATION_STORAGEDEFINITION, null, activatedJob);

        if (sourceFileVariable == null || sourceFileVariable.value == null) {
            throw new ZeebeBpmnError(BPMERROR_LOAD_FILE_ERROR, "Worker [" + getName() + "] cannot read file[" + sourceStorageDefinition + "]");
        }

        Map<String, Object> variables = getInputMapValue(INPUT_VARIABLES, Collections.emptyMap(), activatedJob);
        String listVariablesToAdd = getInputStringValue(INPUT_VARIABLES_NAMES, "", activatedJob);

        // get the file
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourceFileVariable.value);

        final IXDocReport report;
        try {
            report = XDocReportRegistry.getRegistry()
                    .loadReport(byteArrayInputStream, TemplateEngineKind.Velocity);

            final IContext context = report.createContext();

            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
            // add all the additional information
            StringTokenizer st = new StringTokenizer(listVariablesToAdd, ";");
            while (st.hasMoreTokens()) {
                String variableName = st.nextToken();
                Object variableValue = activatedJob.getVariablesAsMap().get(variableName);
                if (variableValue != null)
                    context.put(variableName, variableValue);
            }

            final ByteArrayOutputStream outDoc = new ByteArrayOutputStream();
            report.process(context, outDoc);


            FileVariable fileVariableOut = new FileVariable();
            fileVariableOut.value = outDoc.toByteArray();
            fileVariableOut.name = destinationFileName;
            setFileVariableValue(OUTPUT_DESTINATION_FILE, destinationStorageDefinition, fileVariableOut, contextExecution);
        } catch (Exception e) {
            throw new ZeebeBpmnError(BPMERROR_CONVERSION_ERROR, "Worker [" + getName() + "] cannot generate file[" + sourceFileVariable.name + "] : " + e);
        }

    }
}
