package view.classes;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import model.AM.PwcOdmGradingTerryAMImpl;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCDataControl;
import oracle.adf.view.rich.component.rich.input.RichInputText;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.RowSetIterator;
import oracle.jbo.ViewObject;
import oracle.jbo.server.ApplicationModuleImpl;

public class PwcGradingTerryManagedBean {
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
//        Double totalQty = Double.parseDouble();
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currHeadersVO = am.findViewObject("PwcOdmNGGradingHeadersVO2");
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
                Double totalQty = Double.parseDouble(currRow.getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
                Double gradeAQty = Double.parseDouble(currRow.getAttribute("Gradea")!=null?currRow.getAttribute("Gradea").toString():"0.0");
                Double gradeBQty = Double.parseDouble(currRow.getAttribute("Gradeb")!=null?currRow.getAttribute("Gradeb").toString():"0.0");
                Double gradeCQty = Double.parseDouble(currRow.getAttribute("Gradec")!=null?currRow.getAttribute("Gradec").toString():"0.0");
                Double rewashQty = Double.parseDouble(currRow.getAttribute("Rewash")!=null?currRow.getAttribute("Rewash").toString():"0.0");
                Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
                System.out.println("totalGradeQuantities = "+totalGradeQuantities);
                    if (totalGradeQuantities.compareTo(totalQty)!=0) {
                        String message = "Sum of Grade Quantities must be equal to Total Quantity";
                        showMessage(message, 112);
                        isValid = false;
                        break;
                    }      
            }
            if (isValid)
                am.getDBTransaction().commit();
        }
        System.out.println("sumTotalQtyValue = "+sumTotalQtyValue);
    }
    
    
    /*public void gradeAQtyValidator(FacesContext facesContext,
                                   UIComponent uIComponent, Object object) {
        // Add event code here...
        Double gradeAQty = Double.parseDouble(object.toString());
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        Double totalQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
        Double gradeBQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradeb")!=null?currVO.getCurrentRow().getAttribute("Gradeb").toString():"0.0");
        Double gradeCQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradec")!=null?currVO.getCurrentRow().getAttribute("Gradec").toString():"0.0");
        Double rewashQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Rewash")!=null?currVO.getCurrentRow().getAttribute("Rewash").toString():"0.0");
        Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
        System.out.println("totalGradeQuantities = "+totalGradeQuantities);
            if (totalGradeQuantities.compareTo(totalQty)==1) {
                FacesMessage message =
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Invalid Value",
                       "Total Grade Quantities cannot exceed the Total Quantity");
                       facesContext.addMessage(uIComponent.getClientId(facesContext),
                       message);
                ((RichInputText)uIComponent).setValid(false);
            }
    } 
    
    public void gradeBQtyValidator(FacesContext facesContext,
                                   UIComponent uIComponent, Object object) {
        // Add event code here...
        Double gradeBQty = Double.parseDouble(object.toString());
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        Double totalQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
        Double gradeAQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradea")!=null?currVO.getCurrentRow().getAttribute("Gradea").toString():"0.0");
        Double gradeCQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradec")!=null?currVO.getCurrentRow().getAttribute("Gradec").toString():"0.0");
        Double rewashQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Rewash")!=null?currVO.getCurrentRow().getAttribute("Rewash").toString():"0.0");
        Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
        System.out.println("totalGradeQuantities = "+totalGradeQuantities);
            if (totalGradeQuantities.compareTo(totalQty)==1) {
                FacesMessage message =
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Invalid Value",
                       "Total Grade Quantities cannot exceed the Total Quantity");
                       facesContext.addMessage(uIComponent.getClientId(facesContext),
                       message);
                ((RichInputText)uIComponent).setValid(false);
            }
    } 
    
    public void gradeCQtyValidator(FacesContext facesContext,
                                   UIComponent uIComponent, Object object) {
        // Add event code here...
        Double gradeCQty = Double.parseDouble(object.toString());
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        Double totalQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
        Double gradeAQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradea")!=null?currVO.getCurrentRow().getAttribute("Gradea").toString():"0.0");
        Double gradeBQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradeb")!=null?currVO.getCurrentRow().getAttribute("Gradeb").toString():"0.0");
        Double rewashQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Rewash")!=null?currVO.getCurrentRow().getAttribute("Rewash").toString():"0.0");
        Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
        System.out.println("totalGradeQuantities = "+totalGradeQuantities);
            if (totalGradeQuantities.compareTo(totalQty)==1) {
                FacesMessage message =
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Invalid Value",
                       "Total Grade Quantities cannot exceed the Total Quantity");
                       facesContext.addMessage(uIComponent.getClientId(facesContext),
                       message);
                ((RichInputText)uIComponent).setValid(false);
            }
    } 
    
    public void rewashQtyValidator(FacesContext facesContext,
                                   UIComponent uIComponent, Object object) {
        // Add event code here...
        Double rewashQty = Double.parseDouble(object.toString());
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject currVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        Double totalQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("TotalQuantity")!=null?currVO.getCurrentRow().getAttribute("TotalQuantity").toString():"0.0");
        Double gradeAQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradea")!=null?currVO.getCurrentRow().getAttribute("Gradea").toString():"0.0");
        Double gradeBQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradeb")!=null?currVO.getCurrentRow().getAttribute("Gradeb").toString():"0.0");
        Double gradeCQty = Double.parseDouble(currVO.getCurrentRow().getAttribute("Gradec")!=null?currVO.getCurrentRow().getAttribute("Gradec").toString():"0.0");
        Double totalGradeQuantities = gradeAQty + gradeBQty + gradeCQty + rewashQty;
        System.out.println("totalGradeQuantities = "+totalGradeQuantities);
            if (totalGradeQuantities.compareTo(totalQty)==1) {
                FacesMessage message =
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Invalid Value",
                       "Total Grade Quantities cannot exceed the Total Quantity");
                       facesContext.addMessage(uIComponent.getClientId(facesContext),
                       message);
                ((RichInputText)uIComponent).setValid(false);
            }
    }*/
    
    public void deleteSelectedRows(ActionEvent actionEvent) {
        ApplicationModuleImpl am = getApplicationModule();
        ViewObject linesVO = am.findViewObject("PwcOdmNGGradingLinesVO2");
        RowSetIterator rsi = linesVO.createRowSetIterator(null);
        while (rsi.next()!=null) {
            Row currRow = rsi.getCurrentRow();
            if (currRow.getAttribute("SelectedRow")!=null)
            {
                if ((Boolean)currRow.getAttribute("SelectedRow")==true)
                    currRow.remove();
            }
        }
        rsi.closeRowSetIterator();
        am.getDBTransaction().commit();
    }
}
