document.addEventListener("DOMContentLoaded", function() {

    // Получаем значение скрытого input и проверяем, равно ли оно строке 'true'
    var linkExists = document.getElementById("linkExistsValue").value === 'true';

    if(linkExists) {
        document.getElementById("copyLink").style.display = 'block';
    } else {
        document.getElementById("copyLink").style.display = 'none';
    }

    document.getElementById("linkCreate").addEventListener("submit", function(event) {
        event.preventDefault(); // Предотвращаем отправку формы

        // Получаем CSRF-токен и его заголовок из мета-тегов
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

        fetch('/api/get-link', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken // Добавляем CSRF-токен в заголовок
            },
            body: JSON.stringify({
            }) // Пустое тело, мне не нужно передавать данные
        })
        .then(response => {
              if (!response.ok) {
                  throw new Error('Сетевая ошибка!');
              }
              return response.json(); // Преобразование ответа в JSON
              })
        .then(data => {
            document.getElementById("textLinkToReg").textContent = data.registeredLink; // Заполняем поле сгенерированной ссылкой
            document.getElementById("createLink").style.display = 'none';
            document.getElementById("limitLink").textContent = data.limitLink;
            document.getElementById("copyLink").style.display = 'block';
            //document.getElementById("limitLink").style.display = 'block';
        })
        .catch(error => console.error('Error:', error));
    });

    document.getElementById("copyLink").addEventListener("click", function() {
        // Находим элемент с текстом
        var copyText = document.getElementById("textLinkToReg").textContent;

        // Попробуем использовать Clipboard API
        navigator.clipboard.writeText(copyText).then(function() {
            // Успешное копирование
            alert("Ссылка скопирована: " + copyText);
        }).catch(function(err) {
            // Если возникла ошибка при использовании Clipboard API, используем старый метод
            var tempInput = document.createElement("textarea");
            tempInput.value = copyText;
            document.body.appendChild(tempInput);
            tempInput.select();
            tempInput.setSelectionRange(0, 99999); // Для мобильных устройств
            document.execCommand("copy");
            document.body.removeChild(tempInput);

            alert("Ссылка скопирована: " + copyText);
        });
    });

});
