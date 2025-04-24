package com.ycngmn.nobook.utils

const val fileDownloadScript = """
(function() {
    const originalCreateObjectURL = URL.createObjectURL;
    URL.createObjectURL = function(blob) {
        const reader = new FileReader();
        reader.onloadend = function() {
            if (reader.result) {
                DownloadBridge.downloadBase64File(reader.result, blob.type);
            } else {
                DownloadBridge.showError("Failed to read blob data");
            }
        };
        reader.readAsDataURL(blob);
        return originalCreateObjectURL(blob);
    };
})();"""