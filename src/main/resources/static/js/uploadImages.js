    let imageCount = 1;
    const maxImages = 5;

    // Функция для получения CSRF-токена из мета-тегов
    function getCsrfToken() {
        const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
        return { token, header };
    }

    // Функция для добавления нового input'а для загрузки изображения
    function createNewImageInput() {
        const currentImageCount = document.querySelectorAll('.image-block').length;

        // Проверяем количество изображений
        if (currentImageCount >= maxImages) {
            return; // Не добавляем новый input, если достигнут максимум
        }

        // Если уже есть хотя бы один блок, удаляем у последнего класса 'w-100'
        if (currentImageCount > 0) {
            const lastImageBlock = document.querySelectorAll('.image-block')[currentImageCount - 1];
            lastImageBlock.classList.remove('w-100');
            lastImageBlock.classList.add('flex-xl-column');
        }

        imageCount++; // Увеличиваем счётчик изображений

        const additionalImageInputs = document.getElementById('additionalImageInputs');

        // Создаем контейнер для следующего изображения
        const newImageInput = document.createElement('div');
        newImageInput.classList.add('mb-3', 'me-3', 'col-auto', 'image-block', 'position-relative', 'd-flex', 'w-100');
        newImageInput.id = `imageBlock${imageCount}`;
        newImageInput.innerHTML = `
            <button type="button" class="btn btn-bonus" id="fileButton${imageCount}">+ ФОТО</button>
            <div class="thumb">
            <input type="file" id="productImages${imageCount}" name="productImages[]" class="d-none" accept="image/*">
            <img id="imagePreview${imageCount}" src="#" alt="Предпросмотр изображения ${imageCount}" class="img-thumbnail mb-xl-2 align-self-center" style="display: none;" />
                <div class="spinner-border" id="spinner${imageCount}" role="status" style="display: none;">
                    <span class="visually-hidden">Загрузка...</span>
                </div>
            </div>
            <span id="fileName${imageCount}" class="file-name align-self-center" style="display: none;"></span>
            <button type="button" class="btn btn-close text-reset removeImage align-self-center ms-auto" data-image-id="${imageCount}" style="display: none;"></button>
            <input type="hidden" id="filePath${imageCount}" name="filePath[]" value="" />
        `;

        additionalImageInputs.appendChild(newImageInput);

        const fileButton = document.getElementById(`fileButton${imageCount}`);
        const imageInput = document.getElementById(`productImages${imageCount}`);
        const imagePreview = document.getElementById(`imagePreview${imageCount}`);
        const fileNameSpan = document.getElementById(`fileName${imageCount}`);
        const filePathInput = document.getElementById(`filePath${imageCount}`);
        const removeButton = newImageInput.querySelector('.removeImage');
        const spinner = document.getElementById(`spinner${imageCount}`); // Спиннер

        // При нажатии на кнопку открывается диалог для выбора файла
        fileButton.addEventListener('click', function() {
            imageInput.click();
        });

        // Обработчик для изменения поля file
        imageInput.addEventListener('change', async function() {
            const file = this.files[0];
            if (file) {

            // Проверка типа файла (например, разрешаем только JPEG и PNG)
            const validTypes = ['image/jpeg', 'image/png'];
            if (!validTypes.includes(file.type)) {
                alert('Неверный тип файла. Разрешены только JPEG и PNG.');
                return;
            }

            // Проверка размера файла (например, не больше 5 МБ)
            const maxSize = 5 * 1024 * 1024; // 5 MB
            if (file.size > maxSize) {
                alert('Файл слишком большой. Максимальный размер 5MB.');
                return;
            }

                const reader = new FileReader();

                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    imagePreview.style.display = 'block';
                    fileButton.style.display = 'none';  // Скрываем кнопку "Добавить файл"
                    removeButton.style.display = 'inline-block';  // Показываем кнопку удаления
                    fileNameSpan.textContent = file.name;
                    fileNameSpan.style.display = 'inline-block';

                    // Добавляем новый input только после загрузки изображения
                    if (document.querySelectorAll('.image-block').length < maxImages) {
                        createNewImageInput();
                    } else {
                        const lastImageBlock = document.querySelectorAll('.image-block')[currentImageCount];
                        lastImageBlock.classList.add('flex-xl-column');
                        lastImageBlock.classList.remove('w-100');
                    }
                };


                // Показываем спиннер
                spinner.style.display = 'block';

                // Получаем CSRF-токен
                const csrf = getCsrfToken();

                // Отправляем только выбранный файл на сервер
                const formData = new FormData();
                formData.append('file', file);  // Добавляем файл в FormData
                try {
                const response = await fetch('/api/scan/upload', {  // Убедитесь, что путь правильный
                    method: 'POST',
                    body: formData,
                    headers: {
                        [csrf.header]: csrf.token  // Добавляем CSRF-токен в заголовки
                    }
                });

                const data = await response.json();
                if (response.ok) {
                    const imageUrl = data.imageUrl;
                    // Используйте imageUrl для отображения изображения или других действий
                    // Сохраняем путь файла в скрытом поле
                    filePathInput.value = imageUrl;
                } else {
                    alert(data.error);  // Показываем ошибку
                }
                } catch (error) {
                    console.error("Ошибка при отправке файла:", error);
                    alert("Произошла ошибка при отправке файла.");
                } finally {
                    // Скрываем спиннер после завершения запроса
                    spinner.style.display = 'none';

                }
                reader.readAsDataURL(file);
            }
        });
    }

    // Обновление нумерации блоков после удаления
    function updateImageBlocks() {
        const imageBlocks = document.querySelectorAll('.image-block');
        imageBlocks.forEach((block, index) => {
            const newIndex = index + 1; // Начинаем с 1
            block.id = `imageBlock${newIndex}`;

            const fileButton = block.querySelector('button[id^="fileButton"]');
            fileButton.id = `fileButton${newIndex}`;
            fileButton.textContent = '+ ФОТО';

            const imageInput = block.querySelector('input[type="file"]');
            imageInput.id = `productImages${newIndex}`;
            imageInput.name = `productImages[]`;

            const filePathInput = block.querySelector('input[name="filePath[]"]');
            filePathInput.id = `filePath${newIndex}`;

            const fileNameSpan = block.querySelector('.file-name');
            fileNameSpan.id = `fileName${newIndex}`;

            const imagePreview = block.querySelector('img');
            imagePreview.id = `imagePreview${newIndex}`;
            imagePreview.alt = `Предпросмотр изображения ${newIndex}`;

            const removeButton = block.querySelector('.removeImage');
            removeButton.setAttribute('data-image-id', newIndex);
        });

        imageCount = imageBlocks.length; // Обновляем счётчик
    }

    // Обработчик для кнопок удаления
    document.addEventListener('click', async function(e) {
        if (e.target.classList.contains('removeImage')) {
            const imageId = e.target.getAttribute('data-image-id');
            const imageBlock = document.getElementById(`imageBlock${imageId}`);
            const filePath = document.getElementById(`filePath${imageId}`).value;

        // Отправляем запрос на сервер для удаления файла
        try {
            // Получаем CSRF-токен
            const csrf = getCsrfToken();

            const response = await fetch(`/api/scan/delete`, {
                method: 'DELETE',
                headers: {
                    [csrf.header]: csrf.token,  // Добавляем CSRF-токен в заголовки
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ filePath })  // Передаем путь к файлу
            });
            const data = await response.json();
            if (response.ok) {

                // Удаляем блок с изображением только после успешного удаления на сервере
                imageBlock.remove();
                updateImageBlocks();  // Обновляем нумерацию
            } else {
                alert(data.error || 'Ошибка при удалении файла');
            }
        } catch (error) {
            console.error('Ошибка при отправке запроса на удаление файла:', error);
        }
            imageBlock.remove();  // Удаляем весь блок
            updateImageBlocks();  // Обновляем нумерацию

            // После удаления проверяем, есть ли хоть одна видимая кнопка "+ Файл"
            if (!document.querySelector('button[id^="fileButton"]:not([style*="display: none"])')) {
                // Если нет кнопки "+ ФОТО" и количество блоков меньше максимального, создаем новый input
                createNewImageInput();
            }
        }
    });

    // Добавляем обработчик для первой кнопки
    const firstFileButton = document.getElementById('fileButton1');
    const firstImageInput = document.getElementById('productImages1');
    const firstImagePreview = document.getElementById('imagePreview1');
    const firstFileNameSpan = document.getElementById('fileName1');
    const firstFilePathInput = document.getElementById(`filePath1`);
    const firstRemoveButton = document.querySelector('.removeImage[data-image-id="1"]');
    const firstSpinner = document.getElementById('spinner1'); // Спиннер

    // При нажатии на первую кнопку открывается диалог для выбора файла
    firstFileButton.addEventListener('click', function() {
        firstImageInput.click();
    });

    // Обработчик для изменения первого поля file
    firstImageInput.addEventListener('change', async function() {
        const file = this.files[0];
        if (file) {
            // Проверка типа файла (например, разрешаем только JPEG и PNG)
            const validTypes = ['image/jpeg', 'image/png'];
            if (!validTypes.includes(file.type)) {
                alert('Неверный тип файла. Разрешены только JPEG и PNG.');
                return;
            }
            // Проверка размера файла (например, не больше 5 МБ)
            const maxSize = 5 * 1024 * 1024; // 5 MB
            if (file.size > maxSize) {
                alert('Файл слишком большой. Максимальный размер 5MB.');
                return;
            }
            const reader = new FileReader();

            reader.onload = function(e) {
                // Показать миниатюру, скрыть кнопку + Файл и показать кнопку "Удалить"
                firstImagePreview.src = e.target.result;
                firstImagePreview.style.display = 'block';
                firstFileButton.style.display = 'none';  // Скрываем кнопку "Добавить файл"
                firstRemoveButton.style.display = 'inline-block';  // Показываем кнопку удаления
                firstFileNameSpan.textContent = file.name;
                firstFileNameSpan.style.display = 'block';

                // Добавляем новый input, если не превышен максимум
                if (document.querySelectorAll('.image-block').length < maxImages) {
                    createNewImageInput();
                }
            };

            reader.readAsDataURL(file);

            firstSpinner.style.display = "block";

            // Получаем CSRF-токен
            const csrf = getCsrfToken();

            // Отправляем только выбранный файл на сервер
            const formData = new FormData();
            formData.append('file', file);  // Добавляем файл в FormData
            try {
            const response = await fetch('/api/scan/upload', {  // Убедитесь, что путь правильный
                method: 'POST',
                body: formData,
                headers: {
                    [csrf.header]: csrf.token  // Добавляем CSRF-токен в заголовки
                }
            });

            const data = await response.json();
            if (response.ok) {
                const imageUrl = data.imageUrl;
                // Используйте imageUrl для отображения изображения или других действий
                // Сохраняем путь файла в скрытом поле
                firstFilePathInput.value = imageUrl;
            } else {
                alert(data.error);  // Показываем ошибку
            }
            } catch (error) {
                console.error("Ошибка при отправке файла:", error);
                alert("Произошла ошибка при отправке файла.");
            } finally {
                firstSpinner.style.display = "none";
            }
        }
    });

