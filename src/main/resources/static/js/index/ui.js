loadTheme();

setTheme(themeColor);

function generateGradientColorsHSL(hexColor) {

    function hexToRgb(hex) {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
        return result ? {
            r: parseInt(result[1], 16) / 255,
            g: parseInt(result[2], 16) / 255,
            b: parseInt(result[3], 16) / 255
        } : null;
    }

    function rgbToHsl(r, g, b) {
        const max = Math.max(r, g, b);
        const min = Math.min(r, g, b);
        let h, s, l = (max + min) / 2;

        if (max === min) {
            h = s = 0;
        } else {
            const d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

            switch (max) {
                case r: h = (g - b) / d + (g < b ? 6 : 0); break;
                case g: h = (b - r) / d + 2; break;
                case b: h = (r - g) / d + 4; break;
            }
            h /= 6;
        }

        return { h, s, l };
    }

    function hslToRgb(h, s, l) {
        let r, g, b;

        if (s === 0) {
            r = g = b = l;
        } else {
            const hue2rgb = (p, q, t) => {
                if (t < 0) t += 1;
                if (t > 1) t -= 1;
                if (t < 1/6) return p + (q - p) * 6 * t;
                if (t < 1/2) return q;
                if (t < 2/3) return p + (q - p) * (2/3 - t) * 6;
                return p;
            };

            const q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            const p = 2 * l - q;

            r = hue2rgb(p, q, h + 1/3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1/3);
        }

        return {
            r: Math.round(r * 255),
            g: Math.round(g * 255),
            b: Math.round(b * 255)
        };
    }

    function rgbToHex(r, g, b) {
        return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    }

    const rgb = hexToRgb(hexColor);
    if (!rgb) {
        throw new Error('Неверный формат HEX цвета');
    }

    const hsl = rgbToHsl(rgb.r, rgb.g, rgb.b);

    const colors = [];

    for (let i = 0; i < 11; i++) {
        const lightness = 0.25 + (i * 0.05);

        const rgb = hslToRgb(hsl.h, hsl.s, lightness);
        colors.push(rgbToHex(rgb.r, rgb.g, rgb.b));
    }

    return colors.reverse();
}

function setTheme(baseColor) {
    const root = document.documentElement;

    let gradientColorsHSL = generateGradientColorsHSL(baseColor);

    for (let i = 0; i < 11; i++) {
        root.style.setProperty("--text-" + (i + 1), gradientColorsHSL[i]);
    }
}

function loadTheme() {
    let theme = getCookie("theme");

    if (theme !== undefined) {
        themeColor = theme;
    }
}



window.addEventListener("resize", () => {
    if (window.screen.width <= 730) {
        if (activeChatElement.style.display === "flex") {
            chatsElement.style.display = "none";
        } else {
            chatsElement.style.display = "flex";
            activeChatElement.style.display = "none";
        }
    } else {
        chatsElement.style.display = "flex";
    }
});

menuBgElement.addEventListener('click', (event) => {
    if (event.target === menuBgElement) {
        closeMenu();
    }
});



function copyText(text) {
    return navigator.clipboard.writeText(text);
}

function closeMenu() {
    menuBgElement.style.display = "none";
}

function openMyProfile() {
    menuBgElement.innerHTML = `
        <div class="menu" id="menu">
            <p class="menu-title">Ваш профиль</p>
            <div class="menu-profile">
                <h1>${me.displayName}</h1>
                <p onclick="copyText('${me.tag}')">@${me.tag}</p>
            </div>
            <div class="menu-many-elements">
                <p><span>[</span>Изменить имя<span>]</span></p>
                <p onclick="returnToAuth()"><span>[</span>Выйти<span>]</span></p>
            </div>
        </div>
    `;

    menuBgElement.style.display = "flex";
}

function openOtherProfile() {
    fetch('/app/chat/profile/' + openChatId)
        .then(response => {
            if (!response.ok) {
                returnToAuth();
            }
            return response.json();
        })
        .then(data => {
            menuBgElement.innerHTML = `
                <div class="menu" id="menu">
                    <p class="menu-title">Профиль</p>
                    <div class="menu-profile">
                        <h1>${data.displayName}</h1>
                        <h1>(${data.status})</h1>
                        <p onclick="copyText('${data.tag}')">@${data.tag}</p>
                    </div>
                    <div class="menu-many-elements">
                        <p onclick="createChat()"><span>[</span>Написать<span>]</span></p>
                        <p><span>[</span>Заблокировать<span>]</span></p>
                    </div>
                </div>
            `;

            menuBgElement.style.display = "flex";
        })
        .catch(error => {
            console.error('Fetch error:', error);
        });
}

function openSettings() {
    menuBgElement.innerHTML = `
        <div class="menu" id="menu">
            <p class="menu-title">Настройки</p>
            <div class="menu-many-elements">
                <div class="menu-input">
                    <p>основной цвет темы</p>
                    <div>
                        <input type="color" value="${themeColor}" id="theme">
                    </div>
                </div>
            </div>
        </div>
    `;

    let themeInput = document.getElementById("theme");

    themeInput.addEventListener('input', (event) => {
        setTheme(themeInput.value);
        themeColor = themeInput.value;
    })

    themeInput.addEventListener('change', (event) => {
        setCookie("theme", event.target.value);
    })

    menuBgElement.style.display = "flex";
}

function openCreateChat() {
    menuBgElement.innerHTML = `
        <div class="menu" id="menu">
            <p class="menu-title">Создать чат</p>
            <h1 id="error"></h1>
            <div class="menu-input">
                <p>тег пользователя</p>
                <div>
                    <span>@</span>
                    <input type="text" placeholder="ввод" id="tag">
                </div>
            </div>
            <div class="menu-one-element">
                <p onclick="createChat()"><span>[</span>Написать<span>]</span></p>
            </div>
        </div>
    `;

    document.getElementById("tag").addEventListener("input", function(e) {
        let value = document.getElementById("tag").value;

        if (value.length >= 5 && !/[\d<>/'"$%\[\]#()^&?!=+№;:*`~|\\]/.test(value)) {
            document.getElementById("tag").classList.remove("not-avalable");
        } else {
            document.getElementById("tag").classList.add("not-avalable");
        }
    });

    menuBgElement.style.display = "flex";
}