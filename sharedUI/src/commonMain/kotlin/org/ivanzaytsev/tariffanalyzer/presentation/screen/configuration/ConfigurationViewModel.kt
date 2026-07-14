package org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.ivanzaytsev.tariffanalyzer.domain.usecase.GenerateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.LoadConfigStatusUseCase
import org.ivanzaytsev.tariffanalyzer.domain.usecase.ValidateConfigUseCase
import org.ivanzaytsev.tariffanalyzer.presentation.base.BaseViewModel
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.Action
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.Effect
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.OperationStatus
import org.ivanzaytsev.tariffanalyzer.presentation.screen.configuration.ConfigurationContract.State

class ConfigurationViewModel(
    private val loadConfigStatusUseCase: LoadConfigStatusUseCase,
    private val generateConfigUseCase: GenerateConfigUseCase,
    private val validateConfigUseCase: ValidateConfigUseCase,
) : BaseViewModel<State, Action, Effect>(
    initialState = State(),
    loggerTag = "ConfigurationViewModel",
) {

    init {
        onAction(Action.LoadConfigStatus)
    }

    override fun reduce(action: Action) {
        when (action) {
            Action.LoadConfigStatus -> loadConfigStatus()
            is Action.ChooseTemplatesCsv -> setState {
                it.copy(selectedTemplatesFile = action.file, error = null)
            }

            is Action.ChooseTariffCsv -> setState {
                it.copy(selectedTariffFile = action.file, error = null)
            }

            Action.GenerateConfig -> generateConfig()
            Action.ValidateConfig -> validateConfig()
        }
    }

    private fun loadConfigStatus() {
        setState { it.copy(isLoadingConfigStatus = true, error = null) }
        viewModelScope.launch {
            runCatching { loadConfigStatusUseCase() }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            isLoadingConfigStatus = false,
                            configStatus = result.status,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                        )
                    }
                }
                .onFailure { throwable ->
                    handleFailure(
                        throwable = throwable,
                        fallbackMessage = "Не удалось загрузить статус конфигурации",
                        logOperation = "Loading configuration status",
                    )
                }
        }
    }

    private fun generateConfig() {
        val currentState = state.value
        val templatesFile = currentState.selectedTemplatesFile
        val tariffFile = currentState.selectedTariffFile
        if (templatesFile == null || tariffFile == null) {
            sendEffect(Effect.ShowMessage("Выберите message_templates.csv и tariff.csv"))
            return
        }

        setState { it.copy(operationStatus = OperationStatus.GeneratingConfig, error = null) }
        viewModelScope.launch {
            runCatching { generateConfigUseCase(templatesFile, tariffFile) }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            configStatus = result.status,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                            operationStatus = OperationStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Конфигурация сгенерирована"))
                }
                .onFailure { throwable ->
                    handleFailure(
                        throwable = throwable,
                        fallbackMessage = "Не удалось сгенерировать конфигурацию",
                        logOperation = "Generating configuration",
                    )
                }
        }
    }

    private fun validateConfig() {
        if (state.value.configPath == null) {
            sendEffect(Effect.ShowMessage("Сначала сгенерируйте или загрузите конфигурацию"))
            return
        }

        setState { it.copy(operationStatus = OperationStatus.ValidatingConfig, error = null) }
        viewModelScope.launch {
            runCatching { validateConfigUseCase() }
                .onSuccess { result ->
                    setState {
                        it.copy(
                            configStatus = result.status,
                            configPath = result.configPath,
                            validationIssues = result.issues,
                            operationStatus = OperationStatus.Idle,
                        )
                    }
                    sendEffect(Effect.ShowMessage("Валидация конфигурации завершена"))
                }
                .onFailure { throwable ->
                    handleFailure(
                        throwable = throwable,
                        fallbackMessage = "Не удалось проверить конфигурацию",
                        logOperation = "Validating configuration",
                    )
                }
        }
    }

    private fun handleFailure(
        throwable: Throwable,
        fallbackMessage: String,
        logOperation: String,
    ) {
        val message = throwable.message ?: fallbackMessage
        logError(throwable, logOperation)
        setState {
            it.copy(
                isLoadingConfigStatus = false,
                operationStatus = OperationStatus.Idle,
                error = message,
            )
        }
        sendEffect(Effect.ShowMessage(message))
    }
}
