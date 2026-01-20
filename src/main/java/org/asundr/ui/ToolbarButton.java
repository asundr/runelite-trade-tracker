/*
 * Copyright (c) 2025, Arun <trade-tracker-plugin.acwel@dralias.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.asundr.ui;

import org.asundr.utility.CommonUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Button used for the main panel toolbar that toggles its state when clicked
class ToolbarButton extends JButton
{
    private final ImageIcon iconEnabled, iconDisabled;
    private final static int HEADER_ICON_SIZE = 32;
    private final static Border BORDER_TOOLBAR_BUTTON = BorderFactory.createLineBorder(new Color(30,30,35), 1, true);
    private final static Color COLOR_TOOLBAR_BUTTON_BACKGROUND = new Color(15, 15, 35);

    private final String enabledToolTipText;
    private final String disabledToolTipText;
    private Consumer<Boolean> onToggledActive;      // Called whenever the active state is toggled
    private Predicate<Boolean> onToggledValidate;   // Called to check if a click should toggle the state
    private boolean active;

    public ToolbarButton(final String enabledIconPath, final String disabledIconPath, final String enabledToolTipText, final String disabledToolTipText, final boolean startActive, Consumer<Boolean> onToggledActive)
    {
        super();
        this.active = startActive;
        this.onToggledActive = onToggledActive;
        this.iconEnabled = CommonUtils.getIconFromName(enabledIconPath, HEADER_ICON_SIZE, HEADER_ICON_SIZE, Image.SCALE_SMOOTH);
        this.iconDisabled = disabledIconPath == null ? this.iconEnabled : CommonUtils.getIconFromName(disabledIconPath, HEADER_ICON_SIZE, HEADER_ICON_SIZE, Image.SCALE_SMOOTH);
        setBackground(COLOR_TOOLBAR_BUTTON_BACKGROUND);
        setBorder(BORDER_TOOLBAR_BUTTON);
        setIcon(active ? iconEnabled : iconDisabled);
        this.enabledToolTipText = enabledToolTipText;
        this.disabledToolTipText = disabledToolTipText == null ? this.enabledToolTipText : disabledToolTipText;
        setToolTipText(active ? enabledToolTipText : disabledToolTipText);
        addActionListener(this::actionListenerCallback);
    }

    // Sets the state of the button conditional on onToggledValidate if set
    public void setActive(final boolean newActive)
    {
        if (active == newActive || onToggledValidate != null && !onToggledValidate.test(newActive))
        {
            return;
        }
        active = newActive;
        setIcon(active ? iconEnabled : iconDisabled);
        setToolTipText(active ? enabledToolTipText : disabledToolTipText);
        if (onToggledActive != null)
        {
            onToggledActive.accept(active);
        }
    }
    // Flips the state of the button conditional on onToggledValidate if set
    public void toggleActive()
    {
        setActive(!active);
    }

    public final boolean isActive() { return active; }

    // Sets the callback for when the active state changes
    public void setOnToggledActive(final Consumer<Boolean> onToggledActive)
    {
        this.onToggledActive = onToggledActive;
    }

    // Sets the predicate for determine if changing the active state if valid
    public void setOnToggledValidate(final Predicate<Boolean> onToggledValidate)
    {
        this.onToggledValidate = onToggledValidate;
    }

    protected void actionListenerCallback(ActionEvent e)
    {
        toggleActive();
    }

}
