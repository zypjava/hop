/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.ui.core.dialog;

import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.config.HopConfig;
import org.apache.hop.core.config.plugin.ConfigPluginType;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.util.EnvUtil;
import org.apache.hop.core.util.TranslateUtil;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.i18n.GlobalMessages;
import org.apache.hop.i18n.LanguageChoice;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiCompositeWidgets;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.gui.IGuiPluginCompositeWidgetsListener;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/** Allows you to set the configurable options for the Hop environment */
public class EnterOptionsDialog extends Dialog {
  private static final Class<?> PKG = EnterOptionsDialog.class; // For Translator

  public static final String GUI_WIDGETS_PARENT_ID = "EnterOptionsDialog-GuiWidgetsParent";

  public static final String STRING_USAGE_WARNING_PARAMETER = "EnterOptionsRestartWarning";

  private Display display;

  private CTabFolder wTabFolder;

  private FontData defaultFontData;
  private Font defaultFont;
  private FontData fixedFontData;
  private Font fixedFont;
  private FontData graphFontData;
  private Font graphFont;
  private FontData noteFontData;
  private Font noteFont;

  private Canvas wDefaultCanvas;
  private Canvas wFixedCanvas;
  private Canvas wGraphCanvas;
  private Canvas wNoteCanvas;

  private Text wIconSize;

  private Text wLineWidth;

  private Text wDefaultPreview;

  private Text wMiddlePct;

  private Text wGridSize;

  private Button wDarkMode;

  private Button wShowCanvasGrid;
  private Button wHideMenuBar;

  private Button wUseCache;

  private Button wOpenLast;

  private Button wAutoSave;

  private Button wAutoSplit;

  private Button wCopyDistribute;

  private Button wExitWarning;

  private Combo wDefaultLocale;

  private Combo wGlobalZoom;

  private Shell shell;

  private PropsUi props;

  private int middle;

  private int margin;

  private Button wToolTip;

  private Button wHelpTip;

  private Button wbUseDoubleClick;

  private Button wbUseGlobalFileBookmarks;

  private Button wAutoCollapse;

  private Button wbTableOutputSortMappings;

  private void resetNoteFont(Event e) {
    noteFontData = props.getDefaultFontData();
    noteFont.dispose();
    noteFont = new Font(display, noteFontData);
    wNoteCanvas.redraw();
  }

  private class PluginWidgetContents {
    public GuiCompositeWidgets compositeWidgets;
    public Object sourceData;

    public PluginWidgetContents(GuiCompositeWidgets compositeWidgets, Object sourceData) {
      this.compositeWidgets = compositeWidgets;
      this.sourceData = sourceData;
    }
  }

  private List<PluginWidgetContents> pluginWidgetContentsList;

  public EnterOptionsDialog(Shell parent) {
    super(parent, SWT.NONE);
    props = PropsUi.getInstance();
    pluginWidgetContentsList = new ArrayList<>();
  }

  public Props open() {
    Shell parent = getParent();
    display = parent.getDisplay();

    getData();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET | SWT.RESIZE);
    PropsUi.setLook(shell);
    shell.setImage(GuiResource.getInstance().getImageHopUi());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.Title"));

    middle = props.getMiddlePct();
    margin = PropsUi.getMargin();

    wTabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    addGeneralTab();
    addLookTab();
    addTransformsTab();
    addPluginTabs();

    // Some buttons
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin, null);

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(0, 0);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(wOk, -margin);
    wTabFolder.setLayoutData(fdTabFolder);

    // ///////////////////////////////////////////////////////////
    // / END OF TABS
    // ///////////////////////////////////////////////////////////

    // Add listeners
    wOk.addListener(SWT.Selection, e -> ok());
    wCancel.addListener(SWT.Selection, e -> cancel());

    wTabFolder.setSelection(0);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return props;
  }

  private void addLookTab() {
    int h = (int) (40 * props.getZoomFactor());

    // ////////////////////////
    // START OF LOOK TAB///
    // /
    CTabItem wLookTab = new CTabItem(wTabFolder, SWT.NONE);
    wLookTab.setFont(GuiResource.getInstance().getFontDefault());
    wLookTab.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.LookAndFeel.Label"));

    ScrolledComposite sLookComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    sLookComp.setLayout(new FillLayout());

    Composite wLookComp = new Composite(sLookComp, SWT.NONE);
    PropsUi.setLook(wLookComp);

    FormLayout lookLayout = new FormLayout();
    lookLayout.marginWidth = 3;
    lookLayout.marginHeight = 3;
    wLookComp.setLayout(lookLayout);

    // Default font
    int nr = 0;
    {
      Label wlDFont = new Label(wLookComp, SWT.RIGHT);
      wlDFont.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.DefaultFont.Label"));
      PropsUi.setLook(wlDFont);
      FormData fdlDFont = new FormData();
      fdlDFont.left = new FormAttachment(0, 0);
      fdlDFont.right = new FormAttachment(middle, -margin);
      fdlDFont.top = new FormAttachment(0, margin + 10);
      wlDFont.setLayoutData(fdlDFont);

      Button wdDFont = new Button(wLookComp, SWT.PUSH | SWT.CENTER);
      PropsUi.setLook(wdDFont);
      FormData fddDFont = layoutResetOptionButton(wdDFont);
      fddDFont.right = new FormAttachment(100, 0);
      fddDFont.top = new FormAttachment(0, margin);
      fddDFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wdDFont.setLayoutData(fddDFont);
      wdDFont.addListener(SWT.Selection, this::resetDefaultFont);

      Button wbDFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wbDFont);
      FormData fdbDFont = layoutEditOptionButton(wbDFont);
      fdbDFont.right = new FormAttachment(wdDFont, -margin);
      fdbDFont.top = new FormAttachment(0, nr * h + margin);
      fdbDFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wbDFont.setLayoutData(fdbDFont);
      wbDFont.addListener(SWT.Selection, this::editDefaultFont);

      wDefaultCanvas = new Canvas(wLookComp, SWT.BORDER);
      PropsUi.setLook(wDefaultCanvas);
      FormData fdDFont = new FormData();
      fdDFont.left = new FormAttachment(middle, 0);
      fdDFont.right = new FormAttachment(wbDFont, -margin);
      fdDFont.top = new FormAttachment(0, margin);
      fdDFont.bottom = new FormAttachment(0, h);
      wDefaultCanvas.setLayoutData(fdDFont);
      wDefaultCanvas.addPaintListener(this::paintDefaultFont);
      wDefaultCanvas.addListener(SWT.MouseDown, this::editDefaultFont);
    }

    // Fixed font
    nr++;
    {
      Label wlFFont = new Label(wLookComp, SWT.RIGHT);
      wlFFont.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.FixedWidthFont.Label"));
      PropsUi.setLook(wlFFont);
      FormData fdlFFont = new FormData();
      fdlFFont.left = new FormAttachment(0, 0);
      fdlFFont.right = new FormAttachment(middle, -margin);
      fdlFFont.top = new FormAttachment(0, nr * h + margin + 10);
      wlFFont.setLayoutData(fdlFFont);

      Button wdFFont = new Button(wLookComp, SWT.PUSH | SWT.CENTER);
      PropsUi.setLook(wdFFont);
      FormData fddFFont = layoutResetOptionButton(wdFFont);
      fddFFont.right = new FormAttachment(100, 0);
      fddFFont.top = new FormAttachment(0, nr * h + margin);
      fddFFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wdFFont.setLayoutData(fddFFont);
      wdFFont.addListener(SWT.Selection, this::resetFixedFont);

      Button wbFFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wbFFont);
      FormData fdbFFont = layoutEditOptionButton(wbFFont);
      fdbFFont.right = new FormAttachment(wdFFont, -margin);
      fdbFFont.top = new FormAttachment(0, nr * h + margin);
      fdbFFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wbFFont.setLayoutData(fdbFFont);
      wbFFont.addListener(SWT.Selection, this::editFixedFont);

      wFixedCanvas = new Canvas(wLookComp, SWT.BORDER);
      PropsUi.setLook(wFixedCanvas);
      FormData fdFFont = new FormData();
      fdFFont.left = new FormAttachment(middle, 0);
      fdFFont.right = new FormAttachment(wbFFont, -margin);
      fdFFont.top = new FormAttachment(0, nr * h + margin);
      fdFFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wFixedCanvas.setLayoutData(fdFFont);
      wFixedCanvas.addPaintListener(this::paintFixedFont);
      wFixedCanvas.addListener(SWT.MouseDown, this::editFixedFont);
    }

    // Graph font
    nr++;
    {
      Label wlGFont = new Label(wLookComp, SWT.RIGHT);
      wlGFont.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.GraphFont.Label"));
      PropsUi.setLook(wlGFont);
      FormData fdlGFont = new FormData();
      fdlGFont.left = new FormAttachment(0, 0);
      fdlGFont.right = new FormAttachment(middle, -margin);
      fdlGFont.top = new FormAttachment(0, nr * h + margin + 10);
      wlGFont.setLayoutData(fdlGFont);

      Button wdGFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wdGFont);

      FormData fddGFont = layoutResetOptionButton(wdGFont);
      fddGFont.right = new FormAttachment(100, 0);
      fddGFont.top = new FormAttachment(0, nr * h + margin);
      fddGFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wdGFont.setLayoutData(fddGFont);
      wdGFont.addListener(SWT.Selection, this::resetGraphFont);

      Button wbGFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wbGFont);

      FormData fdbGFont = layoutEditOptionButton(wbGFont);
      fdbGFont.right = new FormAttachment(wdGFont, -margin);
      fdbGFont.top = new FormAttachment(0, nr * h + margin);
      fdbGFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wbGFont.setLayoutData(fdbGFont);
      wbGFont.addListener(SWT.Selection, this::editGraphFont);

      wGraphCanvas = new Canvas(wLookComp, SWT.BORDER);
      PropsUi.setLook(wGraphCanvas);
      FormData fdGFont = new FormData();
      fdGFont.left = new FormAttachment(middle, 0);
      fdGFont.right = new FormAttachment(wbGFont, -margin);
      fdGFont.top = new FormAttachment(0, nr * h + margin);
      fdGFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wGraphCanvas.setLayoutData(fdGFont);
      wGraphCanvas.addPaintListener(this::drawGraphFont);
      wGraphCanvas.addListener(SWT.MouseDown, this::editGraphFont);
    }

    // Note font
    nr++;
    {
      Label wlNFont = new Label(wLookComp, SWT.RIGHT);
      wlNFont.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.NoteFont.Label"));
      PropsUi.setLook(wlNFont);
      FormData fdlNFont = new FormData();
      fdlNFont.left = new FormAttachment(0, 0);
      fdlNFont.right = new FormAttachment(middle, -margin);
      fdlNFont.top = new FormAttachment(0, nr * h + margin + 10);
      wlNFont.setLayoutData(fdlNFont);

      Button wdNFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wdNFont);

      FormData fddNFont = layoutResetOptionButton(wdNFont);
      fddNFont.right = new FormAttachment(100, 0);
      fddNFont.top = new FormAttachment(0, nr * h + margin);
      fddNFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wdNFont.setLayoutData(fddNFont);
      wdNFont.addListener(SWT.Selection, this::resetNoteFont);

      Button wbNFont = new Button(wLookComp, SWT.PUSH);
      PropsUi.setLook(wbNFont);

      FormData fdbNFont = layoutEditOptionButton(wbNFont);
      fdbNFont.right = new FormAttachment(wdNFont, -margin);
      fdbNFont.top = new FormAttachment(0, nr * h + margin);
      fdbNFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wbNFont.setLayoutData(fdbNFont);
      wbNFont.addListener(SWT.Selection, this::editNoteFont);

      wNoteCanvas = new Canvas(wLookComp, SWT.BORDER);
      PropsUi.setLook(wNoteCanvas);
      FormData fdNFont = new FormData();
      fdNFont.left = new FormAttachment(middle, 0);
      fdNFont.right = new FormAttachment(wbNFont, -margin);
      fdNFont.top = new FormAttachment(0, nr * h + margin);
      fdNFont.bottom = new FormAttachment(0, (nr + 1) * h + margin);
      wNoteCanvas.setLayoutData(fdNFont);
      wNoteCanvas.addPaintListener(this::paintNoteFont);
      wNoteCanvas.addListener(SWT.MouseDown, this::editNoteFont);
    }

    // IconSize line
    Label wlIconSize = new Label(wLookComp, SWT.RIGHT);
    wlIconSize.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.IconSize.Label"));
    PropsUi.setLook(wlIconSize);
    FormData fdlIconSize = new FormData();
    fdlIconSize.left = new FormAttachment(0, 0);
    fdlIconSize.right = new FormAttachment(middle, -margin);
    fdlIconSize.top = new FormAttachment(wNoteCanvas, margin);
    wlIconSize.setLayoutData(fdlIconSize);
    wIconSize = new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wIconSize.setText(Integer.toString(props.getIconSize()));
    PropsUi.setLook(wIconSize);
    FormData fdIconSize = new FormData();
    fdIconSize.left = new FormAttachment(middle, 0);
    fdIconSize.right = new FormAttachment(100, -margin);
    fdIconSize.top = new FormAttachment(wlIconSize, 0, SWT.CENTER);
    wIconSize.setLayoutData(fdIconSize);

    // LineWidth line
    Label wlLineWidth = new Label(wLookComp, SWT.RIGHT);
    wlLineWidth.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.LineWidth.Label"));
    PropsUi.setLook(wlLineWidth);
    FormData fdlLineWidth = new FormData();
    fdlLineWidth.left = new FormAttachment(0, 0);
    fdlLineWidth.right = new FormAttachment(middle, -margin);
    fdlLineWidth.top = new FormAttachment(wIconSize, margin);
    wlLineWidth.setLayoutData(fdlLineWidth);
    wLineWidth = new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wLineWidth.setText(Integer.toString(props.getLineWidth()));
    PropsUi.setLook(wLineWidth);
    FormData fdLineWidth = new FormData();
    fdLineWidth.left = new FormAttachment(middle, 0);
    fdLineWidth.right = new FormAttachment(100, -margin);
    fdLineWidth.top = new FormAttachment(wlLineWidth, 0, SWT.CENTER);
    wLineWidth.setLayoutData(fdLineWidth);

    // MiddlePct line
    Label wlMiddlePct = new Label(wLookComp, SWT.RIGHT);
    wlMiddlePct.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.DialogMiddlePercentage.Label"));
    PropsUi.setLook(wlMiddlePct);
    FormData fdlMiddlePct = new FormData();
    fdlMiddlePct.left = new FormAttachment(0, 0);
    fdlMiddlePct.right = new FormAttachment(middle, -margin);
    fdlMiddlePct.top = new FormAttachment(wLineWidth, margin);
    wlMiddlePct.setLayoutData(fdlMiddlePct);
    wMiddlePct = new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wMiddlePct.setText(Integer.toString(props.getMiddlePct()));
    PropsUi.setLook(wMiddlePct);
    FormData fdMiddlePct = new FormData();
    fdMiddlePct.left = new FormAttachment(middle, 0);
    fdMiddlePct.right = new FormAttachment(100, -margin);
    fdMiddlePct.top = new FormAttachment(wlMiddlePct, 0, SWT.CENTER);
    wMiddlePct.setLayoutData(fdMiddlePct);

    // Global Zoom
    Label wlGlobalZoom = new Label(wLookComp, SWT.RIGHT);
    wlGlobalZoom.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.GlobalZoom.Label"));
    PropsUi.setLook(wlGlobalZoom);
    FormData fdlGlobalZoom = new FormData();
    fdlGlobalZoom.left = new FormAttachment(0, 0);
    fdlGlobalZoom.right = new FormAttachment(middle, -margin);
    fdlGlobalZoom.top = new FormAttachment(wMiddlePct, margin);
    wlGlobalZoom.setLayoutData(fdlGlobalZoom);
    wGlobalZoom = new Combo(wLookComp, SWT.SINGLE | SWT.READ_ONLY | SWT.LEFT | SWT.BORDER);
    wGlobalZoom.setItems(PropsUi.getGlobalZoomFactorLevels());
    PropsUi.setLook(wGlobalZoom);
    FormData fdGlobalZoom = new FormData();
    fdGlobalZoom.left = new FormAttachment(middle, 0);
    fdGlobalZoom.right = new FormAttachment(100, -margin);
    fdGlobalZoom.top = new FormAttachment(wlGlobalZoom, 0, SWT.CENTER);
    wGlobalZoom.setLayoutData(fdGlobalZoom);
    // set the current value
    String globalZoomFactor = Integer.toString((int) (props.getGlobalZoomFactor() * 100)) + '%';
    wGlobalZoom.setText(globalZoomFactor);

    // GridSize line
    Label wlGridSize = new Label(wLookComp, SWT.RIGHT);
    wlGridSize.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.GridSize.Label"));
    wlGridSize.setToolTipText(BaseMessages.getString(PKG, "EnterOptionsDialog.GridSize.ToolTip"));
    PropsUi.setLook(wlGridSize);
    FormData fdlGridSize = new FormData();
    fdlGridSize.left = new FormAttachment(0, 0);
    fdlGridSize.right = new FormAttachment(middle, -margin);
    fdlGridSize.top = new FormAttachment(wGlobalZoom, margin);
    wlGridSize.setLayoutData(fdlGridSize);
    wGridSize = new Text(wLookComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wGridSize.setText(Integer.toString(props.getCanvasGridSize()));
    wGridSize.setToolTipText(BaseMessages.getString(PKG, "EnterOptionsDialog.GridSize.ToolTip"));
    PropsUi.setLook(wGridSize);
    FormData fdGridSize = new FormData();
    fdGridSize.left = new FormAttachment(middle, 0);
    fdGridSize.right = new FormAttachment(100, -margin);
    fdGridSize.top = new FormAttachment(wlGridSize, 0, SWT.CENTER);
    wGridSize.setLayoutData(fdGridSize);

    // Show Canvas Grid
    Label wlShowCanvasGrid = new Label(wLookComp, SWT.RIGHT);
    wlShowCanvasGrid.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.ShowCanvasGrid.Label"));
    wlShowCanvasGrid.setToolTipText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.ShowCanvasGrid.ToolTip"));
    PropsUi.setLook(wlShowCanvasGrid);
    FormData fdlShowCanvasGrid = new FormData();
    fdlShowCanvasGrid.left = new FormAttachment(0, 0);
    fdlShowCanvasGrid.right = new FormAttachment(middle, -margin);
    fdlShowCanvasGrid.top = new FormAttachment(wGridSize, margin);
    wlShowCanvasGrid.setLayoutData(fdlShowCanvasGrid);
    wShowCanvasGrid = new Button(wLookComp, SWT.CHECK);
    PropsUi.setLook(wShowCanvasGrid);
    wShowCanvasGrid.setSelection(props.isShowCanvasGridEnabled());
    FormData fdShowCanvasGrid = new FormData();
    fdShowCanvasGrid.left = new FormAttachment(middle, 0);
    fdShowCanvasGrid.right = new FormAttachment(100, -margin);
    fdShowCanvasGrid.top = new FormAttachment(wlShowCanvasGrid, 0, SWT.CENTER);
    wShowCanvasGrid.setLayoutData(fdShowCanvasGrid);

    // Show Canvas Grid
    Label wlHideMenuBar = new Label(wLookComp, SWT.RIGHT);
    wlHideMenuBar.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.HideMenuBar.Label"));
    wlHideMenuBar.setToolTipText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.HideMenuBar.ToolTip"));
    PropsUi.setLook(wlHideMenuBar);
    FormData fdlHideMenuBar = new FormData();
    fdlHideMenuBar.left = new FormAttachment(0, 0);
    fdlHideMenuBar.right = new FormAttachment(middle, -margin);
    fdlHideMenuBar.top = new FormAttachment(wShowCanvasGrid, 2 * margin);
    wlHideMenuBar.setLayoutData(fdlHideMenuBar);
    wHideMenuBar = new Button(wLookComp, SWT.CHECK);
    PropsUi.setLook(wHideMenuBar);
    wHideMenuBar.setSelection(props.isHidingMenuBar());
    FormData fdHideMenuBar = new FormData();
    fdHideMenuBar.left = new FormAttachment(middle, 0);
    fdHideMenuBar.right = new FormAttachment(100, -margin);
    fdHideMenuBar.top = new FormAttachment(wlHideMenuBar, 0, SWT.CENTER);
    wHideMenuBar.setLayoutData(fdHideMenuBar);

    // Is Dark Mode enabled
    Label wlDarkMode = new Label(wLookComp, SWT.RIGHT);
    wlDarkMode.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.DarkMode.Label"));
    PropsUi.setLook(wlDarkMode);
    FormData fdlDarkMode = new FormData();
    fdlDarkMode.left = new FormAttachment(0, 0);
    fdlDarkMode.top = new FormAttachment(wHideMenuBar, 2 * margin);
    fdlDarkMode.right = new FormAttachment(middle, -margin);
    wlDarkMode.setLayoutData(fdlDarkMode);
    wDarkMode = new Button(wLookComp, SWT.CHECK);
    wDarkMode.setSelection(props.isDarkMode());
    PropsUi.setLook(wDarkMode);
    FormData fdDarkMode = new FormData();
    fdDarkMode.left = new FormAttachment(middle, 0);
    fdDarkMode.top = new FormAttachment(wlDarkMode, 0, SWT.CENTER);
    fdDarkMode.right = new FormAttachment(100, 0);
    wDarkMode.setLayoutData(fdDarkMode);
    wlDarkMode.setEnabled(Const.isWindows());
    wDarkMode.setEnabled(Const.isWindows());

    // DefaultLocale line
    Label wlDefaultLocale = new Label(wLookComp, SWT.RIGHT);
    wlDefaultLocale.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.DefaultLocale.Label"));
    PropsUi.setLook(wlDefaultLocale);
    FormData fdlDefaultLocale = new FormData();
    fdlDefaultLocale.left = new FormAttachment(0, 0);
    fdlDefaultLocale.right = new FormAttachment(middle, -margin);
    fdlDefaultLocale.top = new FormAttachment(wlDarkMode, 2 * margin);
    wlDefaultLocale.setLayoutData(fdlDefaultLocale);
    wDefaultLocale = new Combo(wLookComp, SWT.SINGLE | SWT.READ_ONLY | SWT.LEFT | SWT.BORDER);
    wDefaultLocale.setItems(GlobalMessages.localeDescr);
    PropsUi.setLook(wDefaultLocale);
    FormData fdDefaultLocale = new FormData();
    fdDefaultLocale.left = new FormAttachment(middle, 0);
    fdDefaultLocale.right = new FormAttachment(100, -margin);
    fdDefaultLocale.top = new FormAttachment(wlDefaultLocale, 0, SWT.CENTER);
    wDefaultLocale.setLayoutData(fdDefaultLocale);
    // language selections...
    int idxDefault =
        Const.indexOfString(
            LanguageChoice.getInstance().getDefaultLocale().toString(), GlobalMessages.localeCodes);
    if (idxDefault >= 0) {
      wDefaultLocale.select(idxDefault);
    }

    FormData fdLookComp = new FormData();
    fdLookComp.left = new FormAttachment(0, 0);
    fdLookComp.right = new FormAttachment(100, 0);
    fdLookComp.top = new FormAttachment(0, 0);
    fdLookComp.bottom = new FormAttachment(100, 100);
    wLookComp.setLayoutData(fdLookComp);

    wLookComp.pack();

    Rectangle bounds = wLookComp.getBounds();
    sLookComp.setContent(wLookComp);
    sLookComp.setExpandHorizontal(true);
    sLookComp.setExpandVertical(true);
    sLookComp.setMinWidth(bounds.width);
    sLookComp.setMinHeight(bounds.height);

    wLookTab.setControl(sLookComp);

    // ///////////////////////////////////////////////////////////
    // / END OF LOOK TAB
    // ///////////////////////////////////////////////////////////
  }

  private void paintNoteFont(PaintEvent pe) {
    pe.gc.setFont(noteFont);
    Rectangle max = wNoteCanvas.getBounds();
    String name = noteFontData.getName() + " - " + noteFontData.getHeight();
    Point size = pe.gc.textExtent(name);

    pe.gc.drawText(name, (max.width - size.x) / 2, (max.height - size.y) / 2, true);
  }

  private void editNoteFont(Event e) {
    FontDialog fd = new FontDialog(shell);
    fd.setFontList(new FontData[] {noteFontData});
    FontData newfd = fd.open();
    if (newfd != null) {
      noteFontData = newfd;
      noteFont.dispose();
      noteFont = new Font(display, noteFontData);
      wNoteCanvas.redraw();
    }
  }

  private void drawGraphFont(PaintEvent pe) {
    pe.gc.setFont(graphFont);
    Rectangle max = wGraphCanvas.getBounds();
    String name = graphFontData.getName() + " - " + graphFontData.getHeight();
    Point size = pe.gc.textExtent(name);

    pe.gc.drawText(name, (max.width - size.x) / 2, (max.height - size.y) / 2, true);
  }

  private void editGraphFont(Event e) {
    FontDialog fd = new FontDialog(shell);
    fd.setFontList(new FontData[] {graphFontData});
    FontData newfd = fd.open();
    if (newfd != null) {
      graphFontData = newfd;
      graphFont.dispose();
      graphFont = new Font(display, graphFontData);
      wGraphCanvas.redraw();
    }
  }

  private void resetGraphFont(Event e) {
    graphFont.dispose();

    graphFontData = props.getDefaultFontData();
    graphFont = new Font(display, graphFontData);
    wGraphCanvas.redraw();
  }

  private void resetFixedFont(Event e) {
    fixedFontData =
        new FontData(
            PropsUi.getInstance().getFixedFont().getName(),
            PropsUi.getInstance().getFixedFont().getHeight(),
            PropsUi.getInstance().getFixedFont().getStyle());
    fixedFont.dispose();
    fixedFont = new Font(display, fixedFontData);
    wFixedCanvas.redraw();
  }

  private void editFixedFont(Event e) {
    FontDialog fd = new FontDialog(shell);
    fd.setFontList(new FontData[] {fixedFontData});
    FontData newfd = fd.open();
    if (newfd != null) {
      fixedFontData = newfd;
      fixedFont.dispose();
      fixedFont = new Font(display, fixedFontData);
      wFixedCanvas.redraw();
    }
  }

  private void paintFixedFont(PaintEvent pe) {
    pe.gc.setFont(fixedFont);
    Rectangle max = wFixedCanvas.getBounds();
    String name = fixedFontData.getName() + " - " + fixedFontData.getHeight();
    Point size = pe.gc.textExtent(name);

    pe.gc.drawText(name, (max.width - size.x) / 2, (max.height - size.y) / 2, true);
  }

  private void resetDefaultFont(Event e) {
    defaultFontData =
        new FontData(
            PropsUi.getInstance().getFixedFont().getName(),
            PropsUi.getInstance().getFixedFont().getHeight(),
            PropsUi.getInstance().getFixedFont().getStyle());
    defaultFont.dispose();
    defaultFont = new Font(display, defaultFontData);
    wDefaultCanvas.redraw();
  }

  private void paintDefaultFont(PaintEvent pe) {
    pe.gc.setFont(defaultFont);
    Rectangle max = wDefaultCanvas.getBounds();
    String name = defaultFontData.getName() + " - " + defaultFontData.getHeight();
    Point size = pe.gc.textExtent(name);

    pe.gc.drawText(name, (max.width - size.x) / 2, (max.height - size.y) / 2, true);
  }

  private void editDefaultFont(Event e) {
    FontDialog fd = new FontDialog(shell);
    fd.setFontList(new FontData[] {defaultFontData});
    FontData newfd = fd.open();
    if (newfd != null) {
      defaultFontData = newfd;
      defaultFont.dispose();
      defaultFont = new Font(display, defaultFontData);
      wDefaultCanvas.redraw();
    }
  }

  private void addTransformsTab() {
    // ////////////////////////
    // START OF TRANSFORMS TAB///
    // /

    CTabItem wTransformTab = new CTabItem(wTabFolder, SWT.NONE);
    wTransformTab.setFont(GuiResource.getInstance().getFontDefault());
    wTransformTab.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.Transform.Label"));

    FormLayout transformLayout = new FormLayout();
    transformLayout.marginWidth = 3;
    transformLayout.marginHeight = 3;

    ScrolledComposite sTransformComp =
        new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    sTransformComp.setLayout(new FillLayout());

    Composite wTransformComp = new Composite(sTransformComp, SWT.NONE);
    PropsUi.setLook(wTransformComp);
    wTransformComp.setLayout(transformLayout);

    // Use DB Cache?
    Label wlTableOutputSortMappings = new Label(wTransformComp, SWT.RIGHT);
    wlTableOutputSortMappings.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.TableOutput.SortMappings.Label"));
    PropsUi.setLook(wlTableOutputSortMappings);
    FormData fdlSortMappings = new FormData();
    fdlSortMappings.left = new FormAttachment(0, 0);
    fdlSortMappings.top = new FormAttachment(0, margin);
    fdlSortMappings.right = new FormAttachment(middle, -margin);
    wlTableOutputSortMappings.setLayoutData(fdlSortMappings);

    wbTableOutputSortMappings = new Button(wTransformComp, SWT.CHECK);
    PropsUi.setLook(wbTableOutputSortMappings);
    wbTableOutputSortMappings.setSelection(props.useDBCache());
    FormData fdUseCache = new FormData();
    fdUseCache.left = new FormAttachment(middle, 0);
    fdUseCache.top = new FormAttachment(wlTableOutputSortMappings, 0, SWT.CENTER);
    fdUseCache.right = new FormAttachment(100, 0);
    wbTableOutputSortMappings.setLayoutData(fdUseCache);

    wbTableOutputSortMappings.setSelection(props.sortTableOutputMappings());

    FormData fdTransformsComp = new FormData();
    fdTransformsComp.left = new FormAttachment(0, 0);
    fdTransformsComp.right = new FormAttachment(100, 0);
    fdTransformsComp.top = new FormAttachment(0, 0);
    fdTransformsComp.bottom = new FormAttachment(100, 100);
    wTransformComp.setLayoutData(fdTransformsComp);

    wTransformComp.pack();

    Rectangle bounds = wTransformComp.getBounds();

    sTransformComp.setContent(wTransformComp);
    sTransformComp.setExpandHorizontal(true);
    sTransformComp.setExpandVertical(true);
    sTransformComp.setMinWidth(bounds.width);
    sTransformComp.setMinHeight(bounds.height);

    wTransformTab.setControl(sTransformComp);

    // ///////////////////////////////////////////////////////////
    // / END OF TRANSFORMS TAB
    // ///////////////////////////////////////////////////////////
  }

  private void addGeneralTab() {
    // ////////////////////////
    // START OF GENERAL TAB///
    // /
    CTabItem wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
    wGeneralTab.setFont(GuiResource.getInstance().getFontDefault());
    wGeneralTab.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.General.Label"));

    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;

    ScrolledComposite sGeneralComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
    sGeneralComp.setLayout(new FillLayout());

    Composite wGeneralComp = new Composite(sGeneralComp, SWT.NONE);
    PropsUi.setLook(wGeneralComp);
    wGeneralComp.setLayout(generalLayout);

    // Default preview size
    Label wlFilename = new Label(wGeneralComp, SWT.RIGHT);
    wlFilename.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.ConfigFilename.Label"));
    PropsUi.setLook(wlFilename);
    FormData fdlFilename = new FormData();
    fdlFilename.left = new FormAttachment(0, 0);
    fdlFilename.right = new FormAttachment(middle, -margin);
    fdlFilename.top = new FormAttachment(0, margin);
    wlFilename.setLayoutData(fdlFilename);
    Text wFilename = new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wFilename.setText(Const.NVL(HopConfig.getInstance().getConfigFilename(), ""));
    wFilename.setEditable(false);
    PropsUi.setLook(wFilename);
    FormData fdFilename = new FormData();
    fdFilename.left = new FormAttachment(middle, 0);
    fdFilename.right = new FormAttachment(100, -margin);
    fdFilename.top = new FormAttachment(0, margin);
    wFilename.setLayoutData(fdFilename);
    Control lastControl = wFilename;

    // Default preview size
    Label wlDefaultPreview = new Label(wGeneralComp, SWT.RIGHT);
    wlDefaultPreview.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.DefaultPreviewSize.Label"));
    PropsUi.setLook(wlDefaultPreview);
    FormData fdlDefaultPreview = new FormData();
    fdlDefaultPreview.left = new FormAttachment(0, 0);
    fdlDefaultPreview.right = new FormAttachment(middle, -margin);
    fdlDefaultPreview.top = new FormAttachment(lastControl, margin);
    wlDefaultPreview.setLayoutData(fdlDefaultPreview);
    wDefaultPreview = new Text(wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wDefaultPreview.setText(Integer.toString(props.getDefaultPreviewSize()));
    PropsUi.setLook(wDefaultPreview);
    FormData fdDefaultPreview = new FormData();
    fdDefaultPreview.left = new FormAttachment(middle, 0);
    fdDefaultPreview.right = new FormAttachment(100, -margin);
    fdDefaultPreview.top = new FormAttachment(wlDefaultPreview, 0, SWT.CENTER);
    wDefaultPreview.setLayoutData(fdDefaultPreview);
    lastControl = wDefaultPreview;

    // Use DB Cache?
    Label wlUseCache = new Label(wGeneralComp, SWT.RIGHT);
    wlUseCache.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.UseDatabaseCache.Label"));
    PropsUi.setLook(wlUseCache);
    FormData fdlUseCache = new FormData();
    fdlUseCache.left = new FormAttachment(0, 0);
    fdlUseCache.top = new FormAttachment(lastControl, margin);
    fdlUseCache.right = new FormAttachment(middle, -margin);
    wlUseCache.setLayoutData(fdlUseCache);
    wUseCache = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wUseCache);
    wUseCache.setSelection(props.useDBCache());
    FormData fdUseCache = new FormData();
    fdUseCache.left = new FormAttachment(middle, 0);
    fdUseCache.top = new FormAttachment(wlUseCache, 0, SWT.CENTER);
    fdUseCache.right = new FormAttachment(100, 0);
    wUseCache.setLayoutData(fdUseCache);
    lastControl = wlUseCache;

    // Auto load last file at startup?
    Label wlOpenLast = new Label(wGeneralComp, SWT.RIGHT);
    wlOpenLast.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.OpenLastFileStartup.Label"));
    PropsUi.setLook(wlOpenLast);
    FormData fdlOpenLast = new FormData();
    fdlOpenLast.left = new FormAttachment(0, 0);
    fdlOpenLast.top = new FormAttachment(lastControl, margin);
    fdlOpenLast.right = new FormAttachment(middle, -margin);
    wlOpenLast.setLayoutData(fdlOpenLast);
    wOpenLast = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wOpenLast);
    wOpenLast.setSelection(props.openLastFile());
    FormData fdOpenLast = new FormData();
    fdOpenLast.left = new FormAttachment(middle, 0);
    fdOpenLast.top = new FormAttachment(wlOpenLast, 0, SWT.CENTER);
    fdOpenLast.right = new FormAttachment(100, 0);
    wOpenLast.setLayoutData(fdOpenLast);
    lastControl = wlOpenLast;

    // Auto save changed files?
    Label wlAutoSave = new Label(wGeneralComp, SWT.RIGHT);
    wlAutoSave.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.AutoSave.Label"));
    PropsUi.setLook(wlAutoSave);
    FormData fdlAutoSave = new FormData();
    fdlAutoSave.left = new FormAttachment(0, 0);
    fdlAutoSave.top = new FormAttachment(lastControl, margin);
    fdlAutoSave.right = new FormAttachment(middle, -margin);
    wlAutoSave.setLayoutData(fdlAutoSave);
    wAutoSave = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wAutoSave);
    wAutoSave.setSelection(props.getAutoSave());
    FormData fdAutoSave = new FormData();
    fdAutoSave.left = new FormAttachment(middle, 0);
    fdAutoSave.top = new FormAttachment(wlAutoSave, 0, SWT.CENTER);
    fdAutoSave.right = new FormAttachment(100, 0);
    wAutoSave.setLayoutData(fdAutoSave);
    lastControl = wlAutoSave;

    // Automatically split hops?
    Label wlAutoSplit = new Label(wGeneralComp, SWT.RIGHT);
    wlAutoSplit.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.AutoSplitHops.Label"));
    PropsUi.setLook(wlAutoSplit);
    FormData fdlAutoSplit = new FormData();
    fdlAutoSplit.left = new FormAttachment(0, 0);
    fdlAutoSplit.top = new FormAttachment(lastControl, margin);
    fdlAutoSplit.right = new FormAttachment(middle, -margin);
    wlAutoSplit.setLayoutData(fdlAutoSplit);
    wAutoSplit = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wAutoSplit);
    wAutoSplit.setToolTipText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.AutoSplitHops.Tooltip"));
    wAutoSplit.setSelection(props.getAutoSplit());
    FormData fdAutoSplit = new FormData();
    fdAutoSplit.left = new FormAttachment(middle, 0);
    fdAutoSplit.top = new FormAttachment(wlAutoSplit, 0, SWT.CENTER);
    fdAutoSplit.right = new FormAttachment(100, 0);
    wAutoSplit.setLayoutData(fdAutoSplit);
    lastControl = wlAutoSplit;

    // Show warning for copy / distribute...
    Label wlCopyDistrib = new Label(wGeneralComp, SWT.RIGHT);
    wlCopyDistrib.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.CopyOrDistributeDialog.Label"));
    PropsUi.setLook(wlCopyDistrib);
    FormData fdlCopyDistrib = new FormData();
    fdlCopyDistrib.left = new FormAttachment(0, 0);
    fdlCopyDistrib.top = new FormAttachment(lastControl, margin);
    fdlCopyDistrib.right = new FormAttachment(middle, -margin);
    wlCopyDistrib.setLayoutData(fdlCopyDistrib);
    wCopyDistribute = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wCopyDistribute);
    wCopyDistribute.setToolTipText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.CopyOrDistributeDialog.Tooltip"));
    wCopyDistribute.setSelection(props.showCopyOrDistributeWarning());
    FormData fdCopyDistrib = new FormData();
    fdCopyDistrib.left = new FormAttachment(middle, 0);
    fdCopyDistrib.top = new FormAttachment(wlCopyDistrib, 0, SWT.CENTER);
    fdCopyDistrib.right = new FormAttachment(100, 0);
    wCopyDistribute.setLayoutData(fdCopyDistrib);
    lastControl = wlCopyDistrib;

    // Show exit warning?
    Label wlExitWarning = new Label(wGeneralComp, SWT.RIGHT);
    wlExitWarning.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.AskOnExit.Label"));
    PropsUi.setLook(wlExitWarning);
    FormData fdlExitWarning = new FormData();
    fdlExitWarning.left = new FormAttachment(0, 0);
    fdlExitWarning.top = new FormAttachment(lastControl, margin);
    fdlExitWarning.right = new FormAttachment(middle, -margin);
    wlExitWarning.setLayoutData(fdlExitWarning);
    wExitWarning = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wExitWarning);
    wExitWarning.setSelection(props.showExitWarning());
    FormData fdExitWarning = new FormData();
    fdExitWarning.left = new FormAttachment(middle, 0);
    fdExitWarning.top = new FormAttachment(wlExitWarning, 0, SWT.CENTER);
    fdExitWarning.right = new FormAttachment(100, 0);
    wExitWarning.setLayoutData(fdExitWarning);
    lastControl = wlExitWarning;

    // Clear custom parameters. (from transform)
    Label wlClearCustom = new Label(wGeneralComp, SWT.RIGHT);
    wlClearCustom.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.ClearCustomParameters.Label"));
    PropsUi.setLook(wlClearCustom);
    FormData fdlClearCustom = new FormData();
    fdlClearCustom.left = new FormAttachment(0, 0);
    fdlClearCustom.top = new FormAttachment(lastControl, margin + 10);
    fdlClearCustom.right = new FormAttachment(middle, -margin);
    wlClearCustom.setLayoutData(fdlClearCustom);

    Button wClearCustom = new Button(wGeneralComp, SWT.PUSH);
    PropsUi.setLook(wClearCustom);
    FormData fdClearCustom = layoutResetOptionButton(wClearCustom);
    fdClearCustom.width = fdClearCustom.width + 6;
    fdClearCustom.height = fdClearCustom.height + 18;
    fdClearCustom.left = new FormAttachment(middle, 0);
    fdClearCustom.top = new FormAttachment(wlClearCustom, 0, SWT.CENTER);
    wClearCustom.setLayoutData(fdClearCustom);
    wClearCustom.setToolTipText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.ClearCustomParameters.Tooltip"));
    wClearCustom.addListener(
        SWT.Selection,
        e -> {
          MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
          mb.setMessage(
              BaseMessages.getString(PKG, "EnterOptionsDialog.ClearCustomParameters.Question"));
          mb.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.ClearCustomParameters.Title"));
          int id = mb.open();
          if (id == SWT.YES) {
            try {
              props.clearCustomParameters();
              MessageBox ok = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
              ok.setMessage(
                  BaseMessages.getString(
                      PKG, "EnterOptionsDialog.ClearCustomParameters.Confirmation"));
              ok.open();
            } catch (Exception ex) {
              new ErrorDialog(
                  shell, "Error", "Error clearing custom parameters, saving config file", ex);
            }
          }
        });
    lastControl = wClearCustom;

    // Auto-collapse core objects tree branches?
    Label wlAutoCollapse = new Label(wGeneralComp, SWT.RIGHT);
    wlAutoCollapse.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.EnableAutoCollapseCoreObjectTree.Label"));
    PropsUi.setLook(wlAutoCollapse);
    FormData fdlAutoCollapse = new FormData();
    fdlAutoCollapse.left = new FormAttachment(0, 0);
    fdlAutoCollapse.top = new FormAttachment(lastControl, 2 * margin);
    fdlAutoCollapse.right = new FormAttachment(middle, -margin);
    wlAutoCollapse.setLayoutData(fdlAutoCollapse);
    wAutoCollapse = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wAutoCollapse);
    wAutoCollapse.setSelection(props.getAutoCollapseCoreObjectsTree());
    FormData fdAutoCollapse = new FormData();
    fdAutoCollapse.left = new FormAttachment(middle, 0);
    fdAutoCollapse.top = new FormAttachment(wlAutoCollapse, 0, SWT.CENTER);
    fdAutoCollapse.right = new FormAttachment(100, 0);
    wAutoCollapse.setLayoutData(fdAutoCollapse);
    lastControl = wlAutoCollapse;

    // Tooltips
    Label wlToolTip = new Label(wGeneralComp, SWT.RIGHT);
    wlToolTip.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.ToolTipsEnabled.Label"));
    PropsUi.setLook(wlToolTip);
    FormData fdlToolTip = new FormData();
    fdlToolTip.left = new FormAttachment(0, 0);
    fdlToolTip.top = new FormAttachment(lastControl, margin);
    fdlToolTip.right = new FormAttachment(middle, -margin);
    wlToolTip.setLayoutData(fdlToolTip);
    wToolTip = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wToolTip);
    wToolTip.setSelection(props.showToolTips());
    FormData fdbToolTip = new FormData();
    fdbToolTip.left = new FormAttachment(middle, 0);
    fdbToolTip.top = new FormAttachment(wlToolTip, 0, SWT.CENTER);
    fdbToolTip.right = new FormAttachment(100, 0);
    wToolTip.setLayoutData(fdbToolTip);
    lastControl = wlToolTip;

    // Help tool tips
    Label wlHelpTip = new Label(wGeneralComp, SWT.RIGHT);
    wlHelpTip.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.HelpToolTipsEnabled.Label"));
    PropsUi.setLook(wlHelpTip);
    FormData fdlHelpTip = new FormData();
    fdlHelpTip.left = new FormAttachment(0, 0);
    fdlHelpTip.top = new FormAttachment(lastControl, margin);
    fdlHelpTip.right = new FormAttachment(middle, -margin);
    wlHelpTip.setLayoutData(fdlHelpTip);
    wHelpTip = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wHelpTip);
    wHelpTip.setSelection(props.isShowingHelpToolTips());
    FormData fdbHelpTip = new FormData();
    fdbHelpTip.left = new FormAttachment(middle, 0);
    fdbHelpTip.top = new FormAttachment(wlHelpTip, 0, SWT.CENTER);
    fdbHelpTip.right = new FormAttachment(100, 0);
    wHelpTip.setLayoutData(fdbHelpTip);
    lastControl = wlHelpTip;

    // Help tool tips
    Label wlUseDoubleClick = new Label(wGeneralComp, SWT.RIGHT);
    wlUseDoubleClick.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.UseDoubleClickOnCanvas.Label"));
    PropsUi.setLook(wlUseDoubleClick);
    FormData fdlUseDoubleClick = new FormData();
    fdlUseDoubleClick.left = new FormAttachment(0, 0);
    fdlUseDoubleClick.top = new FormAttachment(lastControl, margin);
    fdlUseDoubleClick.right = new FormAttachment(middle, -margin);
    wlUseDoubleClick.setLayoutData(fdlUseDoubleClick);
    wbUseDoubleClick = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wbUseDoubleClick);
    wbUseDoubleClick.setSelection(props.useDoubleClick());
    FormData fdbUseDoubleClick = new FormData();
    fdbUseDoubleClick.left = new FormAttachment(middle, 0);
    fdbUseDoubleClick.top = new FormAttachment(wlUseDoubleClick, 0, SWT.CENTER);
    fdbUseDoubleClick.right = new FormAttachment(100, 0);
    wbUseDoubleClick.setLayoutData(fdbUseDoubleClick);
    lastControl = wlUseDoubleClick;

    // Use global file bookmarks?
    Label wlUseGlobalFileBookmarks = new Label(wGeneralComp, SWT.RIGHT);
    wlUseGlobalFileBookmarks.setText(
        BaseMessages.getString(PKG, "EnterOptionsDialog.UseGlobalFileBookmarks.Label"));
    PropsUi.setLook(wlUseGlobalFileBookmarks);
    FormData fdlUseGlobalFileBookmarks = new FormData();
    fdlUseGlobalFileBookmarks.left = new FormAttachment(0, 0);
    fdlUseGlobalFileBookmarks.top = new FormAttachment(lastControl, margin);
    fdlUseGlobalFileBookmarks.right = new FormAttachment(middle, -margin);
    wlUseGlobalFileBookmarks.setLayoutData(fdlUseGlobalFileBookmarks);
    wbUseGlobalFileBookmarks = new Button(wGeneralComp, SWT.CHECK);
    PropsUi.setLook(wbUseGlobalFileBookmarks);
    wbUseGlobalFileBookmarks.setSelection(props.useGlobalFileBookmarks());
    FormData fdbUseGlobalFileBookmarks = new FormData();
    fdbUseGlobalFileBookmarks.left = new FormAttachment(middle, 0);
    fdbUseGlobalFileBookmarks.top = new FormAttachment(wlUseGlobalFileBookmarks, 0, SWT.CENTER);
    fdbUseGlobalFileBookmarks.right = new FormAttachment(100, 0);
    wbUseGlobalFileBookmarks.setLayoutData(fdbUseGlobalFileBookmarks);

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment(0, 0);
    fdGeneralComp.right = new FormAttachment(100, 0);
    fdGeneralComp.top = new FormAttachment(0, 0);
    fdGeneralComp.bottom = new FormAttachment(100, 100);
    wGeneralComp.setLayoutData(fdGeneralComp);

    wGeneralComp.pack();

    Rectangle bounds = wGeneralComp.getBounds();

    sGeneralComp.setContent(wGeneralComp);
    sGeneralComp.setExpandHorizontal(true);
    sGeneralComp.setExpandVertical(true);
    sGeneralComp.setMinWidth(bounds.width);
    sGeneralComp.setMinHeight(bounds.height);

    wGeneralTab.setControl(sGeneralComp);

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

  }

  private void addPluginTabs() {

    // Add a new tab for every config plugin which is also a GuiPlugin
    // Then simply add the widgets on a separate tab
    //
    HopGui hopGui = HopGui.getInstance();
    PluginRegistry pluginRegistry = PluginRegistry.getInstance();

    List<IPlugin> configPlugins = pluginRegistry.getPlugins(ConfigPluginType.class);
    for (IPlugin configPlugin : configPlugins) {
      try {
        Object emptySourceData = pluginRegistry.loadClass(configPlugin);
        GuiPlugin annotation = emptySourceData.getClass().getAnnotation(GuiPlugin.class);
        if (annotation != null) {

          // Load the instance
          //
          Method method = emptySourceData.getClass().getMethod("getInstance");
          Object sourceData = method.invoke(null, (Object[]) null);

          // This config plugin is also a GUI plugin
          // Add a tab
          //
          CTabItem wPluginTab = new CTabItem(wTabFolder, SWT.NONE);
          wPluginTab.setFont(GuiResource.getInstance().getFontDefault());
          wPluginTab.setText(
              Const.NVL(
                  TranslateUtil.translate(annotation.description(), emptySourceData.getClass()),
                  ""));

          ScrolledComposite sOtherComp =
              new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL);
          sOtherComp.setLayout(new FormLayout());

          Composite wPluginsComp = new Composite(sOtherComp, SWT.NONE);
          PropsUi.setLook(wPluginsComp);
          wPluginsComp.setLayout(new FormLayout());

          GuiCompositeWidgets compositeWidgets = new GuiCompositeWidgets(hopGui.getVariables());
          compositeWidgets.createCompositeWidgets(
              sourceData, null, wPluginsComp, GUI_WIDGETS_PARENT_ID, null);
          compositeWidgets.setWidgetsContents(sourceData, wPluginsComp, GUI_WIDGETS_PARENT_ID);
          if (sourceData instanceof IGuiPluginCompositeWidgetsListener) {
            compositeWidgets.setWidgetsListener((IGuiPluginCompositeWidgetsListener) sourceData);
          }

          pluginWidgetContentsList.add(new PluginWidgetContents(compositeWidgets, sourceData));

          wPluginsComp.pack();

          Rectangle bounds = wPluginsComp.getBounds();

          sOtherComp.setContent(wPluginsComp);
          sOtherComp.setExpandHorizontal(true);
          sOtherComp.setExpandVertical(true);
          sOtherComp.setMinWidth(bounds.width);
          sOtherComp.setMinHeight(bounds.height);

          wPluginTab.setControl(sOtherComp);
        }

      } catch (Exception e) {
        new ErrorDialog(
            shell,
            "Error",
            "Error handling configuration options for config / GUI plugin "
                + configPlugin.getIds()[0],
            e);
      }

      // ///////////////////////////////////////////////////////////
      // / END OF PLUGINS TAB
      // ///////////////////////////////////////////////////////////

    }
  }

  /**
   * Setting the layout of a <i>Reset</i> option button. Either a button image is set - if existing
   * - or a text.
   *
   * @param button The button
   */
  private FormData layoutResetOptionButton(Button button) {
    FormData fd = new FormData();
    Image editButton = GuiResource.getInstance().getImageResetOption();
    if (editButton != null) {
      button.setImage(editButton);
      button.setBackground(GuiResource.getInstance().getColorWhite());
      fd.width = editButton.getBounds().width + 20;
      fd.height = editButton.getBounds().height;
    } else {
      button.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.Button.Reset"));
    }

    button.setToolTipText(BaseMessages.getString(PKG, "EnterOptionsDialog.Button.Reset.Tooltip"));
    return fd;
  }

  /**
   * Setting the layout of an <i>Edit</i> option button. Either a button image is set - if existing
   * - or a text.
   *
   * @param button The button
   */
  private FormData layoutEditOptionButton(Button button) {
    FormData fd = new FormData();
    Image editButton = GuiResource.getInstance().getImageEdit();
    if (editButton != null) {
      button.setImage(editButton);
      button.setBackground(GuiResource.getInstance().getColorWhite());
      fd.width = editButton.getBounds().width + 20;
      fd.height = editButton.getBounds().height;
    } else {
      button.setText(BaseMessages.getString(PKG, "EnterOptionsDialog.Button.Edit"));
    }

    button.setToolTipText(BaseMessages.getString(PKG, "EnterOptionsDialog.Button.Edit.Tooltip"));
    return fd;
  }

  public void dispose() {
    fixedFont.dispose();
    graphFont.dispose();
    noteFont.dispose();

    shell.dispose();
  }

  public void getData() {
    defaultFontData = props.getDefaultFont();
    defaultFont = new Font(display, defaultFontData);

    fixedFontData = props.getFixedFont();
    fixedFont = new Font(display, fixedFontData);

    graphFontData = props.getGraphFont();
    graphFont = new Font(display, graphFontData);

    noteFontData = props.getNoteFont();
    noteFont = new Font(display, noteFontData);
  }

  private void cancel() {
    props.setScreen(new WindowProperty(shell));
    props = null;
    dispose();
  }

  private void ok() {
    props.setDefaultFont(defaultFontData);
    props.setFixedFont(fixedFontData);
    props.setGraphFont(graphFontData);
    props.setNoteFont(noteFontData);
    props.setIconSize(Const.toInt(wIconSize.getText(), props.getIconSize()));
    props.setLineWidth(Const.toInt(wLineWidth.getText(), props.getLineWidth()));
    props.setMiddlePct(Const.toInt(wMiddlePct.getText(), props.getMiddlePct()));
    props.setCanvasGridSize(Const.toInt(wGridSize.getText(), 1));

    props.setDefaultPreviewSize(
        Const.toInt(wDefaultPreview.getText(), props.getDefaultPreviewSize()));
    props.setGlobalZoomFactor(Const.toDouble(wGlobalZoom.getText().replace("%", ""), 100) / 100);

    props.setUseDBCache(wUseCache.getSelection());
    props.setOpenLastFile(wOpenLast.getSelection());
    props.setAutoSave(wAutoSave.getSelection());
    props.setAutoSplit(wAutoSplit.getSelection());
    props.setShowCopyOrDistributeWarning(wCopyDistribute.getSelection());
    props.setShowCanvasGridEnabled(wShowCanvasGrid.getSelection());
    props.setExitWarningShown(wExitWarning.getSelection());
    props.setDarkMode(wDarkMode.getSelection());
    props.setShowToolTips(wToolTip.getSelection());
    props.setHidingMenuBar(wHideMenuBar.getSelection());
    props.setAutoCollapseCoreObjectsTree(wAutoCollapse.getSelection());
    props.setShowingHelpToolTips(wHelpTip.getSelection());
    props.setUseDoubleClickOnCanvas(wbUseDoubleClick.getSelection());
    props.setUseGlobalFileBookmarks(wbUseGlobalFileBookmarks.getSelection());

    props.setTableOutputSortMappings(wbTableOutputSortMappings.getSelection());

    int defaultLocaleIndex = wDefaultLocale.getSelectionIndex();
    if (defaultLocaleIndex < 0 || defaultLocaleIndex >= GlobalMessages.localeCodes.length) {
      // Code hardening, when the combo-box ever gets in a strange state,
      // use the first language as default (should be English)
      defaultLocaleIndex = 0;
    }

    String defaultLocale = GlobalMessages.localeCodes[defaultLocaleIndex];
    LanguageChoice.getInstance().setDefaultLocale(EnvUtil.createLocale(defaultLocale));

    // Persist the plugin configuration options as well...
    //
    for (PluginWidgetContents contents : pluginWidgetContentsList) {
      if (contents.sourceData instanceof IGuiPluginCompositeWidgetsListener) {
        ((IGuiPluginCompositeWidgetsListener) contents.sourceData)
            .persistContents(contents.compositeWidgets);
      }
    }

    if ("Y".equalsIgnoreCase(props.getCustomParameter(STRING_USAGE_WARNING_PARAMETER, "Y"))) {
      MessageDialogWithToggle md =
          new MessageDialogWithToggle(
              shell,
              BaseMessages.getString(PKG, "EnterOptionsDialog.RestartWarning.DialogTitle"),
              BaseMessages.getString(
                      PKG, "EnterOptionsDialog.RestartWarning.DialogMessage", Const.CR)
                  + Const.CR,
              SWT.ICON_WARNING,
              new String[] {
                BaseMessages.getString(PKG, "EnterOptionsDialog.RestartWarning.Option1")
              },
              BaseMessages.getString(PKG, "EnterOptionsDialog.RestartWarning.Option2"),
              "N".equalsIgnoreCase(props.getCustomParameter(STRING_USAGE_WARNING_PARAMETER, "Y")));
      md.open();
      props.setCustomParameter(STRING_USAGE_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y");
    }

    dispose();
  }
}
