# .platform/nginx/conf.d/proxy.conf

# Pass the original Authorization header to backend
proxy_set_header Authorization $http_authorization;

# Forward standard headers as usual
proxy_set_header Host $host;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;

# Increase buffer sizes in case large headers are used
proxy_buffer_size   128k;
proxy_buffers   4 256k;
proxy_busy_buffers_size   256k;

# Optional: disable buffering for better real-time behavior
proxy_buffering off;
