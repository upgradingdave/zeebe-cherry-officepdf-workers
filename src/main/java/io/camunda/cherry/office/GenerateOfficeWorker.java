/* ******************************************************************** */
/*                                                                      */
/*  GenerateOfficeWorker                                                */
/*                                                                      */
/*  Generate a office document (Microsoft, OpenOffice) from a template  */
/*  and a dictionary ti replace the place holder in the document        */
/* ******************************************************************** */
package io.camunda.cherry.office;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import io.camunda.cherry.definition.AbstractWorker;
import io.camunda.cherry.definition.BpmnError;
import io.camunda.cherry.definition.RunnerParameter;
import io.camunda.file.storage.FileVariable;
import io.camunda.file.storage.StorageDefinition;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
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
  public static final String WORKERTYPE_OFFICE_GENERATION = "c-office-generation";
  private static final String INPUT_SOURCE_FILE = "sourceFile";
  private static final String INPUT_SOURCE_STORAGEDEFINITION = "sourceStorageDefinition";
  private static final String INPUT_DESTINATION_FILE_NAME = "destinationFileName";
  private static final String INPUT_DESTINATION_STORAGEDEFINITION = "destinationStorageDefinition";
  private static final String INPUT_VARIABLES = "variables";
  private static final String INPUT_VARIABLES_NAMES = "variablesName";
  private static final String OUTPUT_DESTINATION_FILE = "destinationFile";
  private static final String WORKERLOGO = "data:image/svg+xml,%3C?xml version='1.0' encoding='UTF-8' standalone='no'?%3E%3Csvg   xmlns:dc='http://purl.org/dc/elements/1.1/'   xmlns:cc='http://creativecommons.org/ns%23'   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns%23'   xmlns:svg='http://www.w3.org/2000/svg'   xmlns='http://www.w3.org/2000/svg'   viewBox='0 0 18 18'   version='1.1'   id='svg12'   width='18'   height='18'%3E  %3Cmetadata     id='metadata18'%3E    %3Crdf:RDF%3E      %3Ccc:Work         rdf:about=''%3E        %3Cdc:format%3Eimage/svg+xml%3C/dc:format%3E        %3Cdc:type           rdf:resource='http://purl.org/dc/dcmitype/StillImage' /%3E        %3Cdc:title%3E%3C/dc:title%3E      %3C/cc:Work%3E    %3C/rdf:RDF%3E  %3C/metadata%3E  %3Cdefs     id='defs16' /%3E  %3Cpath     fill='%23f3f3f3'     d='M 0,0 H 18 V 18 H 0 Z'     id='path2'     style='stroke-width:0.782609' /%3E  %3Cpath     fill='%23f35325'     d='m 0.7826087,0.7826087 h 7.826087 v 7.826087 h -7.826087 z'     id='path4'     style='stroke-width:0.782609' /%3E  %3Cpath     fill='%2381bc06'     d='m 9.3913043,0.7826087 h 7.8260867 v 7.826087 H 9.3913043 Z'     id='path6'     style='stroke-width:0.782609' /%3E  %3Cpath     fill='%2305a6f0'     d='m 0.7826087,9.3913043 h 7.826087 v 7.8260867 h -7.826087 z'     id='path8'     style='stroke-width:0.782609' /%3E  %3Cpath     fill='%23ffba08'     d='M 9.3913043,9.3913043 H 17.217391 V 17.217391 H 9.3913043 Z'     id='path10'     style='stroke-width:0.782609' /%3E  %3Cg     id='g39'     transform='matrix(0.5955278,0,0,0.5955278,3.6062962,3.6775518)'%3E    %3Cpath       d='m 16.7328,13.29858 c -0.1476,1.8864 -1.6524,2.7918 -1.9584,2.9574 -1.629,0.8892 -4.3704,0.5796 -5.6952001,-1.2852 -0.7308,-1.026 -1.098,-2.6586 -0.387,-3.789 l 0.009,-0.018 c 0.2556,-0.4158 0.8478,-1.1178 1.9404001,-1.1412 h 0.0396 c 0.3906,0 0.8406,0.1188 1.2384,0.2232 0.279,0.0738 0.5202,0.1368 0.7002,0.1494 0.1134,0.0072 0.2754,-0.0216 0.4806,-0.0576 0.6138,-0.108 1.638,-0.288 2.5344,0.3456 1.2204,0.865801 1.1052,2.5452 1.098,2.6154 z'       id='path2-9'       style='fill:%23aa0000;fill-opacity:1' /%3E    %3Cpath       d='m 9.2718,10.287181 c -0.351,0.2484 -0.5832,0.5562 -0.7236,0.783 l -0.009,0.0144 c -0.3636001,0.5796 -0.4644,1.2528 -0.3960001,1.9116 -0.0072,0.0036 -0.0144,0.0072 -0.0198,0.0126 -1.3338001,1.089 -2.8404,0.8082 -3.4254003,0.6372 l -0.0342,-0.009 c -1.8630002,-0.4644 -3.70259996,-2.4768 -3.3642,-4.6206 0.1872,-1.1808001 1.08,-2.538 2.4264,-2.8476 0.2322,-0.054 1.4454,-0.279 2.421,0.5382 0.2988,0.252 0.5202,0.6192001 0.7146,0.9432 0.1386,0.234 0.2592,0.4338 0.3906001,0.5652 0.0846,0.0846 0.2322,0.1674 0.4194,0.2736 0.549,0.3078001 1.3770001,0.7740001 1.5930002,1.7784 0.0018,0.0072 0.0036,0.0144 0.0072,0.0198 z'       id='path4-1'       style='fill:%23aa0000;fill-opacity:1' /%3E    %3Cpath       d='m 12.9294,10.18638 c -0.1062,0.018 -0.1998,0.0306 -0.2664,0.0306 -0.0108,0 -0.0216,-0.0018 -0.0306,-0.0018 -0.0504,-0.0036 -0.1098,-0.0108 -0.1728,-0.0234 0.5526,-1.4112 0.7812,-3.6684 -1.1394,-6.5268 -0.0036,-0.0072 -0.009,-0.0126 -0.0162,-0.018 0.1116,-0.4428 0.1494,-0.7524 0.1584,-0.828 0.0054,-0.0504 -0.0306,-0.0936 -0.0792,-0.099 -0.0504,-0.0072 -0.0936,0.0306 -0.099,0.0792 -0.0216,0.1926 -0.2376,1.9296001 -1.4490001,3.4722004 -0.7578001,0.9648001 -1.9512001,1.6830001 -2.3634002,1.8882 -0.0378,-0.0252 -0.0702,-0.0504 -0.0918,-0.072 -0.0594,-0.0594 -0.117,-0.1368 -0.1764,-0.2286 0.0342,-0.018 0.0702,-0.0378 0.108,-0.0576 0.4302,-0.2304 1.1502,-0.6138001 2.0286002,-1.5336001 1.0386001,-1.0872 1.2366001,-2.7522001 0.9450001,-3.2058 -0.1404,-0.2196 -0.3654001,-0.2412 -0.5454001,-0.2574 -0.2358,-0.0198 -0.378,-0.0342 -0.4032,-0.3546 -0.0234,-0.279 0.108,-0.5256 0.369,-0.6912 0.3510001,-0.225 0.9792001,-0.297 1.5876001,0.0486 0.3798,0.2142 0.3762,0.5796 0.3744,0.9324 -0.0018,0.2358 -0.0036,0.459 0.1134,0.6192 l 0.0504,0.0684 c 0.288,0.3906001 0.9612,1.3014001 1.3644,2.8908 C 13.626,8.01918 13.2192,9.56898 12.9294,10.18638 Z'       id='path6-1'       style='fill:%23502d16;fill-opacity:1' /%3E    %3Cpath       d='m 7.6338,2.8045802 c -0.0144,0.0072 -0.0288,0.0108 -0.0432,0.0108 -0.0324,0 -0.063,-0.018 -0.0792,-0.0468 -0.0018,-0.0018 -0.1278,-0.2268 -0.3546,-0.4338 -0.0378,-0.0324 -0.0396,-0.09 -0.0072,-0.126 0.0342,-0.0378 0.09,-0.0396 0.1278,-0.0072 0.2502,0.2268 0.3852,0.4698 0.3906,0.4806 0.025201,0.0432 0.009,0.0972 -0.0342,0.1224 z'       id='path8-2' /%3E    %3Cpath       d='m 7.8156,4.51998 c -0.1872,0.2214 -0.2844,0.261 -0.432,0.3204 l -0.0468,0.0198 c -0.0108,0.0054 -0.0234,0.0072 -0.0342,0.0072 -0.036,0 -0.0684,-0.0216 -0.0828,-0.0558 -0.0198,-0.045 0.0018,-0.099 0.0486,-0.117 l 0.0468,-0.0198 c 0.135,-0.0558 0.2034,-0.0846 0.3636,-0.27 0.0324,-0.0396 0.09,-0.0432 0.1278,-0.0108 0.0378,0.0324 0.0414,0.0882 0.009,0.126 z'       id='path10-6' /%3E    %3Cpath       d='m 10.134,3.1591802 c -0.0468,-0.0738 -0.108,-0.1134 -0.1764,-0.135 -0.2394,0.3222 -1.0818001,0.81 -1.9278,0.81 -0.225,0 -0.4518,-0.0342 -0.666,-0.1152 -0.576,-0.2196 -0.9702,-0.5256 -1.3518001,-0.8208 -0.3024,-0.2358 -0.5886,-0.4572 -0.9378,-0.6084001 -0.0468,-0.0216 -0.0666,-0.0738 -0.0468,-0.1188 0.0198,-0.0468 0.072,-0.0666 0.1188,-0.0468 0.369,0.162 0.6642,0.3888001 0.9756,0.6318 0.3708,0.2880001 0.756,0.5850001 1.3050001,0.7938 0.5598,0.2124 1.1880001,0.0738 1.6650001,-0.1404 -0.2052,-0.6336 -1.5318002,-2.56139997 -3.7188,-2.0700002 -0.3384,0.0756 -0.6336,0.1458 -0.8928,0.207 -1.098,0.2574 -1.5678,0.3672001 -2.0556002,0.2376 0.045,0.2268001 0.1638,0.4194 0.3528,0.5742001 0.378,0.3042 0.927,0.3798 1.1880001,0.3834 C 3.8322,2.59038 3.69,2.49318 3.5154,2.43198 3.4686,2.41578 3.4434,2.36538 3.4596,2.31858 c 0.0162,-0.0486 0.0684,-0.072 0.1152,-0.0558 0.567,0.1998 0.828,0.7074 1.2492001,1.7424002 0.441,1.0836 1.9836001,2.0934 3.2346,1.7190001 1.2744,-0.3816 1.2528001,-1.7244 1.251,-1.7388 0,-0.036 0.0216,-0.0702 0.054,-0.0846 0.0846,-0.0378 0.5184,-0.2358 0.8243999,-0.5238 0.0054,-0.0054 0.0108,-0.009 0.0162,-0.009 v -0.0018 C 10.1898,3.27798 10.1646,3.20778 10.134,3.1591802 M 7.1496005,2.20878 c 0.0342,-0.0378 0.09,-0.0396 0.1278,-0.0072 0.2502,0.2268 0.3852,0.4698 0.3906,0.4806 0.0252,0.0432 0.009,0.0972 -0.0342,0.1224 -0.0144,0.0072 -0.0288,0.0108 -0.0432,0.0108 -0.0324,0 -0.063,-0.018 -0.0792,-0.0468 -0.0018,-0.0018 -0.1278,-0.2268 -0.3546,-0.4338 -0.037801,-0.0324 -0.0396,-0.09 -0.0072,-0.126 m -1.5156002,1.2258 c -0.0036,0 -0.0558,0.009 -0.1584,0.009 -0.081,0 -0.1908,-0.0054 -0.3312,-0.0234 -0.0504,-0.0054 -0.0846,-0.0504 -0.0774,-0.1008 0.0054,-0.0486 0.0504,-0.0828 0.1008,-0.0774 0.2916,0.0378 0.4338,0.0162 0.4356,0.0162 0.0486,-0.009 0.0954,0.0234 0.1026,0.0738 0.009,0.0486 -0.0234,0.0936 -0.072,0.1026 M 7.8156,4.51998 c -0.1872,0.2214 -0.2844,0.261 -0.432,0.3204 l -0.0468,0.0198 c -0.0108,0.0054 -0.0234,0.0072 -0.0342,0.0072 -0.036,0 -0.0684,-0.0216 -0.0828,-0.0558 -0.0198,-0.045 0.0018,-0.099 0.0486,-0.117 l 0.0468,-0.0198 c 0.135,-0.0558 0.2034,-0.0846 0.3636,-0.27 0.0324,-0.0396 0.09,-0.0432 0.1278,-0.0108 0.0378,0.0324 0.0414,0.0882 0.009,0.126 z'       id='path12'       style='fill:%23008000;fill-opacity:1' /%3E    %3Cpath       d='m 5.7060003,3.33198 c 0.009,0.0486 -0.0234,0.0936 -0.072,0.1026 -0.0036,0 -0.0558,0.009 -0.1584,0.009 -0.081,0 -0.1908,-0.0054 -0.3312,-0.0234 -0.0504,-0.0054 -0.0846,-0.0504 -0.0774,-0.1008 0.0054,-0.0486 0.0504,-0.0828 0.1008,-0.0774 0.2916,0.0378 0.4338,0.0162 0.4356,0.0162 0.0486,-0.009 0.0954,0.0234 0.1026,0.0738 z'       id='path14' /%3E  %3C/g%3E%3C/svg%3E";

  Logger logger = LoggerFactory.getLogger(GenerateOfficeWorker.class.getName());

  public GenerateOfficeWorker() {
    super(WORKERTYPE_OFFICE_GENERATION, Arrays.asList(
            RunnerParameter.getInstance(INPUT_SOURCE_FILE, "Source file", Object.class, RunnerParameter.Level.REQUIRED,
                "FileVariable for the file to convert"),
            RunnerParameter.getInstance(INPUT_SOURCE_STORAGEDEFINITION, "Source Storage definition", String.class,
                StorageDefinition.StorageDefinitionType.JSON.toString(), RunnerParameter.Level.OPTIONAL,
                "Storage Definition use to access the file"),
            RunnerParameter.getInstance(INPUT_DESTINATION_FILE_NAME, "Destination file name", String.class,
                RunnerParameter.Level.REQUIRED, "Destination file name"),
            RunnerParameter.getInstance(INPUT_DESTINATION_STORAGEDEFINITION, "Destination storage defintion", String.class,
                StorageDefinition.StorageDefinitionType.JSON.toString(), RunnerParameter.Level.OPTIONAL,
                "Storage Definition use to describe how to save the file"),
            RunnerParameter.getInstance(INPUT_VARIABLES, "Dictionary variables for place holder", Map.class,
                RunnerParameter.Level.OPTIONAL,
                "Template document contains place holders. This is the dictionary which contains values for theses place holder"),
            RunnerParameter.getInstance(INPUT_VARIABLES_NAMES, "Names of variables in the dictionary", String.class,
                RunnerParameter.Level.OPTIONAL,
                "Template document contains place holders. Here the list of variable to add in the dictionary for place holder")

        ), Collections.singletonList(RunnerParameter.getInstance(OUTPUT_DESTINATION_FILE, "Destination file", Object.class,
            RunnerParameter.Level.REQUIRED, "FileVariable converted")),
        Arrays.asList(BpmnError.getInstance(BPMERROR_CONVERSION_ERROR, "Conversion error"),
            BpmnError.getInstance(BPMERROR_LOAD_FILE_ERROR, "Load file error")));
  }

  @Override
  public String getName() {
    return "Office Generation";
  }

  @Override
  public String getDescription() {
    return "A new office document is generated for an office (Word, OpenOffice) document containing placeholders (Fields in Word) and a dictionary. The Library Velocity is used.";
  }

  @Override
  public String getLogo() {
    return WORKERLOGO;
  }

  @Override
  public void execute(final JobClient jobClient, final ActivatedJob activatedJob, ContextExecution contextExecution) {
    String sourceStorageDefinition = getInputStringValue(INPUT_SOURCE_STORAGEDEFINITION, null, activatedJob);
    FileVariable sourceFileVariable = getInputFileVariableValue(INPUT_SOURCE_FILE, activatedJob);

    String destinationFileName = getInputStringValue(INPUT_DESTINATION_FILE_NAME, null, activatedJob);
    String destinationStorageDefinitionSt = getInputStringValue(INPUT_DESTINATION_STORAGEDEFINITION, null,
        activatedJob);
    try {

      StorageDefinition destinationStorageDefinition = StorageDefinition.getFromString(destinationStorageDefinitionSt);

      if (sourceFileVariable == null || sourceFileVariable.getValue() == null) {
        throw new ZeebeBpmnError(BPMERROR_LOAD_FILE_ERROR,
            "Worker [" + getName() + "] cannot read file[" + sourceStorageDefinition + "]");
      }

      Map<String, Object> variables = (Map<String, Object>) getInputMapValue(INPUT_VARIABLES, Collections.emptyMap(),
          activatedJob);
      String listVariablesToAdd = getInputStringValue(INPUT_VARIABLES_NAMES, "", activatedJob);

      // get the file
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sourceFileVariable.getValue());

      final IXDocReport report;
      report = XDocReportRegistry.getRegistry().loadReport(byteArrayInputStream, TemplateEngineKind.Velocity);

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
      fileVariableOut.setValue(outDoc.toByteArray());
      fileVariableOut.setName(destinationFileName);
      setOutputFileVariableValue(OUTPUT_DESTINATION_FILE, destinationStorageDefinition, fileVariableOut,
          contextExecution);
    } catch (Exception e) {
      throw new ZeebeBpmnError(BPMERROR_CONVERSION_ERROR, "Worker [" + getName() + "] cannot generate file[" + (
          sourceFileVariable == null ?
              "null" :
              sourceFileVariable.getName()) + "] : " + e);
    }

  }

}
