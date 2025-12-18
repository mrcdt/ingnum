<?php
header('Content-Type: text/plain');

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    echo "Marine Cadet";
} else {
    http_response_code(405);
    echo "Method Not Allowed";
}
