-- Roblox Game Tools / Launcher
-- Safe utility for Roblox Studio / experiences you own.
-- This does not bypass Roblox security or modify other people's games.

local Players = game:GetService("Players")
local TeleportService = game:GetService("TeleportService")
local UserInputService = game:GetService("UserInputService")
local RunService = game:GetService("RunService")

local player = Players.LocalPlayer
local gui = Instance.new("ScreenGui")
gui.Name = "GameTools"
gui.ResetOnSpawn = false
gui.Parent = player:WaitForChild("PlayerGui")

local frame = Instance.new("Frame")
frame.Size = UDim2.fromOffset(260, 300)
frame.Position = UDim2.new(0, 20, 0.5, -150)
frame.BackgroundTransparency = 0.08
frame.Parent = gui

local corner = Instance.new("UICorner")
corner.CornerRadius = UDim.new(0, 12)
corner.Parent = frame

local title = Instance.new("TextLabel")
title.Size = UDim2.new(1, 0, 0, 42)
title.BackgroundTransparency = 1
title.Text = "ROBLOX GAME TOOLS"
title.TextScaled = true
title.Font = Enum.Font.GothamBold
title.Parent = frame

local layout = Instance.new("UIListLayout")
layout.Padding = UDim.new(0, 8)
layout.HorizontalAlignment = Enum.HorizontalAlignment.Center
layout.SortOrder = Enum.SortOrder.LayoutOrder
layout.Parent = frame

title.LayoutOrder = 0

local function button(text, order, callback)
    local b = Instance.new("TextButton")
    b.Size = UDim2.fromOffset(220, 38)
    b.Text = text
    b.TextScaled = true
    b.Font = Enum.Font.Gotham
    b.LayoutOrder = order
    b.Parent = frame
    b.Activated:Connect(callback)
    return b
end

button("Rejoin", 1, function()
    TeleportService:Teleport(game.PlaceId, player)
end)

button("Reset Character", 2, function()
    local character = player.Character
    local humanoid = character and character:FindFirstChildOfClass("Humanoid")
    if humanoid then humanoid.Health = 0 end
end)

button("Toggle FPS Counter", 3, function()
    local label = gui:FindFirstChild("FPS")
    if label then
        label:Destroy()
        return
    end

    label = Instance.new("TextLabel")
    label.Name = "FPS"
    label.Size = UDim2.fromOffset(150, 32)
    label.Position = UDim2.new(1, -160, 0, 10)
    label.BackgroundTransparency = 0.2
    label.TextScaled = true
    label.Font = Enum.Font.GothamBold
    label.Parent = gui

    local frames = 0
    local elapsed = 0
    local connection
    connection = RunService.RenderStepped:Connect(function(dt)
        if not label.Parent then
            connection:Disconnect()
            return
        end
        frames += 1
        elapsed += dt
        if elapsed >= 0.5 then
            label.Text = string.format("FPS: %d", math.floor(frames / elapsed + 0.5))
            frames = 0
            elapsed = 0
        end
    end)
end)

button("Hide / Show UI", 4, function()
    frame.Visible = not frame.Visible
end)

-- Drag support for mobile and desktop.
local dragging = false
local dragStart
local startPos

local function update(input)
    local delta = input.Position - dragStart
    frame.Position = UDim2.new(
        startPos.X.Scale, startPos.X.Offset + delta.X,
        startPos.Y.Scale, startPos.Y.Offset + delta.Y
    )
end

title.InputBegan:Connect(function(input)
    if input.UserInputType == Enum.UserInputType.MouseButton1 or input.UserInputType == Enum.UserInputType.Touch then
        dragging = true
        dragStart = input.Position
        startPos = frame.Position
        input.Changed:Connect(function()
            if input.UserInputState == Enum.UserInputState.End then
                dragging = false
            end
        end)
    end
end)

title.InputChanged:Connect(function(input)
    if input.UserInputType == Enum.UserInputType.MouseMovement or input.UserInputType == Enum.UserInputType.Touch then
        UserInputService.InputChanged:Connect(function(changed)
            if dragging and changed == input then
                update(changed)
            end
        end)
    end
end)

print("GameTools loaded for " .. player.Name)
