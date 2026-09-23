require "ISUI/ISModalRichText";

AVPF_ZBCheckModal = ISPanelJoypad:derive("AVPF_ZBCheckModal");

local FONT_HGT_SMALL = getTextManager():getFontHeight(UIFont.Small);
local FONT_HGT_LARGE = getTextManager():getFontHeight(UIFont.Large);
local UI_BORDER_SPACING = 10;
local BUTTON_HGT = FONT_HGT_SMALL + 6;
local JOYPAD_TEX_SIZE = 32;
local BUTTON_PADDING = JOYPAD_TEX_SIZE + UI_BORDER_SPACING*2;
local DEPENDENCY_URL = "https://steamcommunity.com/workshop/filedetails/?id=3619862853";

function AVPF_ZBCheckModal:createChildren()
	self.textureX = UI_BORDER_SPACING;
	self.textureY = UI_BORDER_SPACING*2 + FONT_HGT_LARGE + 1;
	self.textureW = self.texture:getWidth();
	self.textureH = self.texture:getHeight();

	local x = self.textureX + self.textureW;
	local y = self.textureY;
	self.richText = ISRichTextPanel:new(x, y, self.width - x, self.height - UI_BORDER_SPACING*2 - BUTTON_HGT - y - 1);
	self.richText.background = false;
	self.richText.autosetheight = false;
	self.richText.clip = true;
	self.richText.marginRight = self.richText.marginLeft;
	self:addChild(self.richText);
	self.richText:addScrollBars();

	self.richText:setText(getText("UI_AVPF_ZBCheck_Message"));
	self.richText:paginate();
	local closeBtnWidth = BUTTON_PADDING + getTextManager():MeasureStringX(UIFont.Small, getText("UI_AVPF_ZBCheck_Close"));
	local zbBtnWidth = BUTTON_PADDING + getTextManager():MeasureStringX(UIFont.Small, getText("UI_AVPF_ZBCheck_Confirm"));

    local totalButtonsWidth = closeBtnWidth + zbBtnWidth;
    local okX = (self:getWidth() - totalButtonsWidth) / 2;
    local cancelX = okX + zbBtnWidth + 20;

	self.cancel = ISButton:new(
        cancelX,
        self:getHeight() - UI_BORDER_SPACING - BUTTON_HGT - 1,
        closeBtnWidth,
        BUTTON_HGT,
        getText("UI_AVPF_ZBCheck_Close"), self, self.onClose
    );
    self.cancel.internal = "CANCEL";
	self.cancel.anchorTop = false;
	self.cancel.anchorBottom = true;
	self.cancel:initialise();
	self.cancel:instantiate();
	self:addChild(self.cancel);

	self.ok = ISButton:new(
        okX,
        self:getHeight() - UI_BORDER_SPACING - BUTTON_HGT - 1,
        zbBtnWidth,
        BUTTON_HGT,
        getText("UI_AVPF_ZBCheck_Confirm"), self, self.onOK
    );
	self.ok.anchorTop = false;
	self.ok.anchorBottom = true;
	self.ok:initialise();
	self.ok:instantiate();
	self:addChild(self.ok);
end

function AVPF_ZBCheckModal:render()
	ISPanelJoypad.render(self);
	self:drawTextCentre(getText("UI_AVPF_ZBCheck"), self.width / 2, UI_BORDER_SPACING+1, 1, 1, 1, 1, UIFont.Large);
	self:drawTextureScaledAspect(self.texture, self.textureX, self.textureY, self.textureW, self.textureH, 1, 1, 1, 1);
end

function AVPF_ZBCheckModal:onGainJoypadFocus(joypadData)
	ISPanelJoypad.onGainJoypadFocus(self, joypadData);
	self:setISButtonForA(self.ok);
	self:setISButtonForB(self.cancel);
end

function AVPF_ZBCheckModal:onOK(button, x, y)
    openUrl(DEPENDENCY_URL);
	self:setVisible(false);
	self:removeFromUIManager();
end

function AVPF_ZBCheckModal:onClose(button, x, y)
	self:setVisible(false);
	self:removeFromUIManager();
end

function AVPF_ZBCheckModal:new(x, y, width, height)
	local o = ISPanelJoypad.new(self, x, y, width, height);
	o.backgroundColor.a = 0.9;
	o.texture = getTexture("spiffoWarning.png");
	return o;
end

Events.OnMainMenuEnter.Add(function()

    if not AnimatedVehiclePartsFix or not AnimatedVehiclePartsFix.ZB_IS_ACTIVE then
       local width = 700;
        local height = 410;
        local modalPanel = AVPF_ZBCheckModal:new(
                (getCore():getScreenWidth() - width)/2,
                (getCore():getScreenHeight() - height)/2,
                width, height);
        modalPanel:initialise();
        modalPanel:addToUIManager();
        modalPanel:setAlwaysOnTop(true);
    end

    
end);