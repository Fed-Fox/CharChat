inputElement.addEventListener('input', (event) => {
    if (inputElement.value[0] === "/") {
        document.getElementById("commands-tips").style.opacity = "1";
    } else {
        document.getElementById("commands-tips").style.opacity = "0";
    }
});