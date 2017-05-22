package com.jin.action;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiFile;
import com.jin.model.PropertiesKey;
import com.jin.model.ViewPart;
import com.jin.util.ActionUtil;
import com.jin.util.CodeWriter;
import com.jin.util.Utils;
import com.jin.util.ViewSaxHandler;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.List;

/**
 * Created by Jin on 2017/5/20
 */
public class FindViewAction extends BaseGenerateAction {
    private boolean isAddRootView;
    private boolean isViewHolder;
    private String rootViewStr;

    private ViewSaxHandler viewSaxHandler;
    private FindViewDialog findViewDialog;
    private List<ViewPart> viewParts;

    private DefaultTableModel tableModel;

    private PsiFile psiFile;
    private Editor editor;


    public FindViewAction() {
        super(null);
    }

    public FindViewAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        isAddRootView = false;
        isViewHolder = false;
        viewSaxHandler = new ViewSaxHandler();
        if (findViewDialog == null) {
            findViewDialog = new FindViewDialog();
        }
        getViewList(anActionEvent);
        ActionUtil.switchAddM(viewParts, PropertiesComponent.getInstance().getBoolean(PropertiesKey.SAVE_ADD_M_ACTION, false));
        updateTable();
        findViewDialog.setTitle("FindViewByMe");
        findViewDialog.btnCopyCode.setText("OK");
        findViewDialog.setOnClickListener(onClickListener);
        findViewDialog.pack();
        findViewDialog.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.getProject()));
        findViewDialog.setVisible(true);

    }

    /**
     * get views list
     *
     * @param event 触发事件
     */
    private void getViewList(AnActionEvent event) {
        psiFile = event.getData(LangDataKeys.PSI_FILE);
        editor = event.getData(PlatformDataKeys.EDITOR);
        PsiFile layout = Utils.getLayoutFileFromCaret(editor, psiFile);
        if (psiFile == null || editor == null) {
            return;
        }
        String contentStr = psiFile.getText();
        if (layout != null) {
            contentStr = layout.getText();
        }
        if (psiFile.getParent() != null) {
            String javaPath = psiFile.getContainingDirectory().toString().replace("PsiDirectory:", "");
            String javaPathKey = "src" + File.separator + "main" + File.separator + "java";
            int indexOf = javaPath.indexOf(javaPathKey);
            String layoutPath = "";
            if (indexOf != -1) {
                layoutPath = javaPath.substring(0, indexOf) + "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout";
            }
            viewSaxHandler.setLayoutPath(layoutPath);
            viewSaxHandler.setProject(event.getProject());
        }
        viewParts = ActionUtil.getViewPartList(viewSaxHandler, contentStr);
    }


    /**
     * FindViewByMe 对话框回调
     */
    private FindViewDialog.onClickListener onClickListener = new FindViewDialog.onClickListener() {

        public void onUpdateRootView() {
            generateCode();
        }


        public void onOK() {
            new CodeWriter(psiFile, getTargetClass(editor, psiFile), viewParts, isViewHolder, isAddRootView, rootViewStr, editor).execute();
        }


        public void onSelectAll() {
            for (ViewPart viewPart : viewParts) {
                viewPart.setSelected(true);
            }
            updateTable();
        }


        public void onSelectNone() {
            for (ViewPart viewPart : viewParts) {
                viewPart.setSelected(false);
            }
            updateTable();
        }


        public void onNegativeSelect() {
            for (ViewPart viewPart : viewParts) {
                viewPart.setSelected(!viewPart.isSelected());
            }
            updateTable();
        }


        public void onSwitchAddRootView(boolean flag) {
            isAddRootView = flag;
            generateCode();
        }


        public void onSwitchAddM(boolean isAddM) {
            ActionUtil.switchAddM(viewParts, isAddM);
            updateTable();
        }


        public void onSwitchIsViewHolder(boolean viewHolder) {
            isViewHolder = viewHolder;
            generateCode();
        }


        public void onFinish() {
            viewParts = null;
            viewSaxHandler = null;
            findViewDialog = null;
        }
    };


    /**
     * 生成FindViewById代码
     */
    private void generateCode() {
        rootViewStr = findViewDialog.getRootView();
        findViewDialog.setTextCode(ActionUtil.generateCode(viewParts, isViewHolder, isAddRootView, rootViewStr));
    }

    /**
     * 更新 View 表格
     */
    public void updateTable() {
        if (viewParts == null || viewParts.size() == 0) {
            return;
        }
        tableModel = ActionUtil.getTableModel(viewParts, tableModelListener);
        findViewDialog.setModel(tableModel);
        generateCode();
    }

    TableModelListener tableModelListener = new TableModelListener() {

        public void tableChanged(TableModelEvent event) {
            if (tableModel == null) {
                return;
            }
            int row = event.getFirstRow();
            int column = event.getColumn();
            if (column == 0) {
                Boolean isSelected = (Boolean) tableModel.getValueAt(row, column);
                viewSaxHandler.getViewPartList().get(row).setSelected(isSelected);
                FindViewAction.this.generateCode();
            }
        }
    };
}
