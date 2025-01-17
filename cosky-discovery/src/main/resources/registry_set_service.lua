local namespace = KEYS[1];
local serviceId = ARGV[1];

local serviceIdxKey = namespace .. ":svc_idx";
local serviceIdxStatKey = namespace .. ":svc_stat";

local affected = redis.call("sadd", serviceIdxKey, serviceId);

if affected > 0 then
    redis.call("hset", serviceIdxStatKey, serviceId, 0);
end

return affected;
