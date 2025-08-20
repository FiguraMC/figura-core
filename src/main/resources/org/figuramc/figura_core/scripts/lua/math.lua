
--- Linearly interpolate between a and b by a factor of t
function math.lerp(a, b, t)
    return a + (b - a) * t
end

--- Compute the fraction that "v" is between a and b.
--- Restated, compute t such that math.lerp(a, b, t) == v
function math.invLerp(a, b, v)
    return (v - a) / (b - a)
end

--- Clamp val between min and max. Output is undefined if min > max.
function math.clamp(val, min, max)
    return val > max and max or val < min and min or val
end

--- Round the given number to the nearest integer.
function math.round(val)
    return math.floor(val + 0.5)
end

--- Compute what fraction "v" is between min1 and max1, then
--- use that fraction to lerp between min2 and max2.
--- Same as math.lerp(min2, max2, math.invLerp(min1, max1, v))
function math.map(v, min1, max1, min2, max2)
    return min2 + (max2 - min2) * ((v - min1) / (min2 - min1))
end

--- Compute the sign of x, either -1, 0, or 1
--- @param x number
--- @return number
function math.sign(x)
    return x > 0 and 1 or x < 0 and -1 or 0
end

--- The scale factor with which players are rendered by Minecraft, relative to the world
--- @type number
math.playerScale = 0.9375

--- The scale factor with which the world is rendered, relative to players.
--- Just equal to 1 / math.playerScale.
--- @type number
math.worldScale = 1 / math.playerScale