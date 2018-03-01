package view.classes;

import java.sql.Types;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import model.AM.PwcOdmGradingTerryAMImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCDataControl;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.adf.view.rich.event.DialogEvent;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ApplicationModuleImpl;

import oracle.wsm.common.util.CommonUtil;
import oracle.adf.view.rich.event.DialogEvent.Outcome;

public class PwcGradingTerryManagedBean {
    private RichPopup completeJobPopup;
    private RichPopup returnJobPopup;
    public PwcGradingTerryManagedBean() {
        super();
    }
    

    /*****Generic Method to Get BindingContainer**/
    public BindingContainer getBindingsCont() {
     return BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    /**
     * Generic Method to execute operation
     * */
    public OperationBinding executeOperation(String operation) {
    OperationBinding createParam = getBindingsCont().getOperationBinding(operation);
    return createParam;
    }
    
    public static void showMessage(String message, int code) {

            FacesMessage.Severity s = null;
            if (code == 112) {
                s = FacesMessage.SEVERITY_ERROR;
            } else if (code == 111) {
                s = FacesMessage.SEVERITY_INFO;
            }

            FacesMessage msg = new FacesMessage(s, message, "");
            FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public static PwcOdmGradingTerryAMImpl getApplicationModule() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        BindingContext bindingContext = BindingContext.getCurrent();
        DCDataControl dc = bindingContext.findDataControl("PwcOdmGradingTerryAMDataControl");
        return (PwcOdmGradingTerryAMImpl)dc.getDataProvider();
        }
    
    public void validateAndSaveChanges(ActionEvent actionEvent) {
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currHeadersVO = am.findViewObject("PwcOdmNGGradingHeadersVO2");
        if (currHeadersVO.getCurrentRow()!=null)
        {
            Double stitchingQty = Double.parseDouble(currHeadersVO.getCurrentRow().getAttribute("StitchQuantity")!=null?currHeadersVO.getCurrentRow().getAttribute("StitchQuantity").toString():"0.0");
            Double sumTotalQtyValue = Double.parseDouble(currHeadersVO.getCurrentRow().getAttribute("SumTotalQuantity")!=null?currHeadersVO.getCurrentRow().getAttribute("SumTotalQuantity").toString():"0.0");
            if (sumTotalQtyValue.compareTo(stitchingQty)==1) {
                String message =
                       "Sum of Total Quantities cannot exceed Stitching Quantity";
                showMessage(message, 112);
            }
            else {
                ViewObject currVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
                RowSetIterator rsi = currVO.createRowSetIterator(null);
                boolean isValid = true;
                while(rsi.next()!=null)
                {
                    Row currRow = rsi.getCurrentRow();
                    System.out.println("Row = "+rsi.getCurrentRowIndex());
                    Double totalQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
                    Double gradeAQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradea")!=null?currVO.getCurrentRow().getAttribute("Gradea").toString():"0.0");
                    Double gradeBQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradeb")!=null?currVO.getCurrentRow().getAttribute("Gradeb").toString():"0.0");
                    Double gradeCQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradec")!=null?currVO.getCurrentRow().getAttribute("Gradec").toString():"0.0");
                    Double rewashQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Rewash")!=null?currVO.getCurrentRow().getAttribute("Rewash").toString():"0.0");
                    Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
                    System.out.println("totalGradeQuantities = "+totalGradeQuantities);
                    System.out.println("totalQty = "+totalQty);
                        if (totalGradeQuantities.compareTo(totalQty)!=0) {
                            String message = "Sum of Grade Quantities must be equal to Total Quantity";
//                            showMessage(message, 112);
                            isValid = false;
                            break;
                        }      
                }
                if (isValid)
                    am.getDBTransaction().commit();
            }
        }
    }
    
    public void deleteSelectedRows(ActionEvent actionEvent) {
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject linesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        RowSetIterator rsi = linesVO.createRowSetIterator(null);
        while (rsi.next()!=null) {
            Row currRow = rsi.getCurrentRow();
            if (currRow.getAttribute("SelectedRow")!=null)
            {
                if (currRow.getAttribute("RequestStatus")!=null)
                {
                    if(currRow.getAttribute("RequestStatus").equals("S"))
                        showMessage("Completed Line(s) cannot be deleted",112);
                    else 
                    {
                        if ((Boolean)currRow.getAttribute("SelectedRow")==true)
                            currRow.remove();
                    }
                }
                else
                {
                    if ((Boolean)currRow.getAttribute("SelectedRow")==true)
                        currRow.remove();
                }
            }
        }
        rsi.closeRowSetIterator();
        am.getDBTransaction().commit();
    }
    
    public void setCompleteJobPopup(RichPopup completeJobPopup) {
        this.completeJobPopup = completeJobPopup;
    }

    public RichPopup getCompleteJobPopup() {
        return completeJobPopup;
    }
    
    public void setReturnJobPopup(RichPopup returnJobPopup) {
        this.returnJobPopup = returnJobPopup;
    }

    public RichPopup getReturnJobPopup() {
        return returnJobPopup;
    }
    
    public void completeJobAPIActionListener(ActionEvent actionEvent) {
        if (isJobCompleted()) {
            showMessage("Job already completed.", 112);
        }
        else
        {
            RichPopup.PopupHints hints = new RichPopup.PopupHints();
            completeJobPopup.show(hints);
        }
    }
    
    public void returnJobAPIActionListener(ActionEvent actionEvent) {
        if (checkIfAnyRowSelected())
        {
            if (isJobReturned()) {
                showMessage("Job not completed yet", 112);
            }
            else
            {
                RichPopup.PopupHints hints = new RichPopup.PopupHints();
                returnJobPopup.show(hints);
            }
        }
        else showMessage("No line(s) selected", 112);
    }
    
    public Boolean isJobCompleted() {
        Boolean result = true;
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject gradingWaveingLinesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        RowSetIterator rsi = gradingWaveingLinesVO.createRowSetIterator(null);
        if (rsi.getAllRowsInRange().length>0) {
            while (rsi.next()!=null) {
                Row currRow = rsi.getCurrentRow();
                if (currRow.getAttribute("RequestStatus")==null) {
                    result = false;
                    break;
                }
                else if (currRow.getAttribute("RequestStatus").equals("R"))
                {
                    result = false;
                    break;
                }
            }   
        }
        else return false;
        return result;
    }

    public Boolean isJobReturned() {
        Boolean result = true;
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject gradingWaveingLinesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        RowSetIterator rsi = gradingWaveingLinesVO.createRowSetIterator(null);
        while (rsi.next()!=null) {
            Row currRow = rsi.getCurrentRow();
            if ((Boolean)currRow.getAttribute("SelectedRow")==Boolean.TRUE)
            {
                if (currRow.getAttribute("RequestStatus")!=null)
                {
                    if (currRow.getAttribute("RequestStatus").equals("S")) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void completeJobDialogListener(DialogEvent dialogEvent) {
        Outcome outcome = dialogEvent.getOutcome();
        if (outcome == Outcome.yes) {
            ApplicationModuleImpl am = getApplicationModule();
            ViewObject currHeadersVO = am.findViewObject("PwcOdmNGGradingHeadersVO2");
            Row currRow = currHeadersVO.getCurrentRow();
            if (currRow!=null)
            {
                String jobId = currRow.getAttribute("JobId").toString();
                String stmt = "PWC_ODM_WO_LESS_COMPL_TER_API(? " +
                    ",?" +
                    ",?" +
                    ",?" +
                    ",?" +
                    ",?" +          
                    ",?)";
                BindingContainer bindings = getBindingsCont();
                OperationBinding operationBinding = bindings.getOperationBinding("callAPIProc");
                Map params =  operationBinding.getParamsMap();
                params.put("sqlReturnType", Types.VARCHAR);
                params.put("stmt", stmt);
                params.put("requestStatus", "S");
                String result =(String) operationBinding.execute();
                    System.out.println("result = "+result);
                if (result != null) {
                    if (result.equals("SUCCESSFUL"))
                        showMessage("Job Completed Successfully!", 111);
                    else
                        showMessage(result+"", 112);
                }
            }
            else showMessage("No job found", 112);
            completeJobPopup.hide();
        }
        else {
            completeJobPopup.hide();
        }
    }

    public void returnJobDialogListener(DialogEvent dialogEvent) {
        Outcome outcome = dialogEvent.getOutcome();
        if (outcome == Outcome.yes) {
            ApplicationModuleImpl am = getApplicationModule();
            ViewObject currHeadersVO = am.findViewObject("PwcOdmNGGradingHeadersVO2");
            Row currRow = currHeadersVO.getCurrentRow();
            if (currRow!=null)
            {
                String jobId = currRow.getAttribute("JobId").toString();
                String stmt = "PWC_ODM_WO_LESS_RET_TER_API(? " +
                    ",?" +
                    ",?" +
                    ",?" +
                    ",?" +
                    ",?" +          
                    ",?)";
                BindingContainer bindings = getBindingsCont();
                OperationBinding operationBinding = bindings.getOperationBinding("callAPIProc");
                Map params =  operationBinding.getParamsMap();
                params.put("sqlReturnType", Types.VARCHAR);
                params.put("stmt", stmt);
                params.put("requestStatus", "R");
                String result =(String) operationBinding.execute();
                    System.out.println("result = "+result);
                if (result != null) {
                    if (result.equals("SUCCESSFUL"))
                        showMessage("Line(s) Returned Successfully!", 111);
                    else
                        showMessage(result+"", 112);
                }
            }
            else showMessage("No job found", 112);
            returnJobPopup.hide();
        }
        else {
            returnJobPopup.hide();
        }
    }
    
    public Boolean checkIfAnyRowSelected() {
        Boolean result = false;
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject gradingWaveingLinesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        RowSetIterator rsi = gradingWaveingLinesVO.createRowSetIterator(null);
        while (rsi.next()!=null) {
            Row currRow = rsi.getCurrentRow();
            if ((Boolean)currRow.getAttribute("SelectedRow")==Boolean.TRUE)
            {
                result = true;
                break;
            }
        }
        return result;
    }
    
    public void deleteHeaderRow(ActionEvent actionEvent) {
        if (isJobCompleted())
            showMessage("Job cannot be deleted because it's completed", 112);
        else
        {
            ApplicationModuleImpl am = getApplicationModule();
            ViewObject currHeadersVO = am.findViewObject("PwcOdmNGGradingHeadersVO2");
            ViewObject linesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
            RowSetIterator rsi = linesVO.createRowSetIterator(null);
            System.out.println("lines = "+rsi.getAllRowsInRange().length);
            if (linesVO.getAllRowsInRange().length>0)
                showMessage("Delete all lines before deleting the header", 112);
            else
            {
                currHeadersVO.getCurrentRow().remove();
                am.getDBTransaction().commit();
                rsi = currHeadersVO.createRowSetIterator(null);
                Row lastRow = rsi.last();
                int lastRowIndex = rsi.getRangeIndexOf(lastRow);
                Row newRow = rsi.createRow();
                newRow.setNewRowState(Row.STATUS_INITIALIZED);
                rsi.insertRowAtRangeIndex(0, newRow); 
                rsi.setCurrentRow(newRow);      
            }
        }
    }
}
